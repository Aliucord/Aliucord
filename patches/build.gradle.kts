import com.aliucord.gradle.task.adb.DeployComponentTask
import org.gradle.api.internal.file.FileOperations
import org.gradle.kotlin.dsl.support.serviceOf
import java.io.ByteArrayOutputStream
import java.util.Properties

version = "1.3.1"

// --- Android --- //

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.aliucord.patches"
    compileSdk = 36
}

// ------ Dependencies ------ //

// Make dependency configuration for build tools
val smaliTools by configurations.registering
val discord by configurations.registering

dependencies {
    smaliTools(libs.smali)
    smaliTools(libs.smali.baksmali)
    discord(libs.discord)
}

// ------ Shared files ------ //

val patchesDir = projectDir.resolve("src")
val smaliDir = projectDir.resolve("smali")
val smaliOriginalDir = layout.buildDirectory.dir("smali_original")
val patchesBundle = layout.buildDirectory.file("outputs/patches.zip")

// ------ Other ------ //

/** Task group for all public tasks */
@Suppress("PropertyName")
val TASK_GROUP = "aliucord"

/** Task group for all private tasks */
@Suppress("PropertyName")
val TASK_GROUP_INTERNAL = "aliucordInternal"

val localPropertiesFile = project.rootProject.file("local.properties")
val localProperties = Properties().apply {
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

// Regular function notation breaks with configuration caching??
val execIgnoreCode: (ExecOperations.(Logger, ExecSpec.() -> Unit) -> Pair<ExecResult, String>) =
    execIgnoreCode@{ logger, block ->
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()

        val result = exec {
            isIgnoreExitValue = true
            standardOutput = stdout
            errorOutput = stderr
            block(this)
        }

        val stdoutString = stdout.toString()
        val stderrString = stderr.toString()

        if (stderrString.isNotEmpty())
            logger.error(stderrString)

        logger.info(stdoutString)

        return@execIgnoreCode result to stdoutString
    }

// ------ Internal tasks ------ //

val disassembleInternal = tasks.register("disassembleInternal") {
    group = TASK_GROUP_INTERNAL

    // Configuration cache workaround
    val execOperations = serviceOf<ExecOperations>()
    val fileOperations = serviceOf<FileOperations>()
    val smaliOriginalDir = smaliOriginalDir
    val smaliTools = smaliTools.map { it }

    // Resolve Discord APK dependency to an apk file
    val discordApks = discord.map {
        it.incoming
            .artifactView { attributes.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "apk") }
            .files
    }

    inputs.files(smaliTools)
    inputs.files(discordApks)
    outputs.dir(smaliOriginalDir)

    doFirst {
        val smaliOriginalDir = smaliOriginalDir.get().asFile
            .apply { deleteRecursively() }

        val discordApk = discordApks.get().singleFile
        val classesFiles = fileOperations.zipTree(discordApk)
            .matching { include("classes*.dex") }

        classesFiles.visit {
            logger.info("Disassembling $name")
            execOperations.javaexec {
                jvmArgs = listOf("-Xmx2G")
                classpath = smaliTools.get()
                systemProperty("line.separator", "\n") // Ensure smali output uses LF endings

                mainClass.set("com.android.tools.smali.baksmali.Main")
                args = listOf(
                    "disassemble",
                    "--accessor-comments", "false",
                    "--use-locals",
                    "--output", smaliOriginalDir.absolutePath,
                    "${discordApk.absolutePath}/$name",
                )
            }
        }
    }
}

val copyDisassembled by tasks.register("copyDisassembled", Sync::class) {
    group = TASK_GROUP_INTERNAL
    destinationDir = smaliDir
    from(smaliOriginalDir)
    dependsOn(disassembleInternal)
}

val applyPatches = tasks.register("applyPatches") {
    group = TASK_GROUP_INTERNAL

    // Configuration cache workaround
    val execOperations = serviceOf<ExecOperations>()
    val fileOperations = serviceOf<FileOperations>()
    val execIgnoreCode = execIgnoreCode
    val patchesDir = patchesDir
    val smaliDir = smaliDir

    dependsOn(copyDisassembled)
    outputs.upToDateWhen { false }

    val patchBin = localProperties.getProperty("patch.bin", null)
        ?: project.findProperty("patch.bin") as String?
        ?: "patch"

    doLast {
        val patchFiles = fileOperations.fileTree(patchesDir)
            .filter { it.name.endsWith(".patch") }

        for (patchFile in patchFiles) {
            val fileClassName = patchFile
                .toRelativeString(patchesDir)
                .removeSuffix(".patch")

            val className = fileClassName.replace("""[\\/]""".toRegex(), ".")
            logger.lifecycle("Applying smali patches to class $className")

            execOperations.execIgnoreCode(logger) {
                executable = patchBin
                args = listOf(
                    "--verbose",
                    "--forward",
                    "--unified",
                    smaliDir.resolve("$fileClassName.smali").absolutePath,
                    patchFile.absolutePath,
                )
            }
        }
    }
}

// --- Public tasks --- //

tasks.named<Delete>("clean") {
    group = TASK_GROUP
    delete(smaliDir)
}

tasks.register("disassembleWithPatches") {
    group = TASK_GROUP
    dependsOn(disassembleInternal, copyDisassembled, applyPatches)
}

tasks.register<JavaExec>("testPatches") {
    group = TASK_GROUP
    mustRunAfter(applyPatches) // When applyPatches is also being run, it must come before

    // Configuration cache workaround
    val smaliDir = smaliDir
    val outputDex = layout.buildDirectory.file("patched.dex")

    // Get all patch files and their corresponding smali file
    val patchFiles = fileTree(patchesDir) { include("**/*.patch") }
    val smaliFiles = patchFiles.map { file ->
        file.toRelativeString(patchesDir)
            .replace(".patch", ".smali")
            .let(smaliDir::resolve)
    }

    // Up-to-Date config
    inputs.files(patchFiles, smaliFiles)
    outputs.file(outputDex)

    // javaexec config
    classpath(smaliTools)
    jvmArgs = listOf("-Xmx2G")
    mainClass = "com.android.tools.smali.smali.Main"
    args = listOf(
        "assemble",
        "--verbose",
        "--output", outputDex.get().asFile.absolutePath,
    ) + smaliFiles.map { it.absolutePath }

    doFirst {
        if (!smaliDir.exists()) {
            error("Smali directory does not exist! Run the disassembleWithPatches task")
        }
    }
    doLast {
        logger.lifecycle("Successfully reassembled dex: {}", outputDex.get().asFile.absolutePath)
    }
}

val packageTask by tasks.register("package", Zip::class) {
    group = TASK_GROUP
    archiveFileName = patchesBundle.get().asFile.name
    destinationDirectory = patchesBundle.get().asFile.parentFile
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false

    from(patchesDir)
    include(".gitkeep")
    include("**/*.patch")
}

tasks.register<DeployComponentTask>("deployWithAdb") {
    group = TASK_GROUP
    componentType = "patches"
    componentVersion = project.version.toString()
    componentFile.set(packageTask.outputs.files.singleFile)
    dependsOn(packageTask)
}

tasks.register("writePatches") {
    group = TASK_GROUP
    mustRunAfter(applyPatches) // When applyPatches is also being run, it must come before

    // Configuration cache workaround
    val execOperations = serviceOf<ExecOperations>()
    val execIgnoreCode = execIgnoreCode
    val smaliOriginalDir = smaliOriginalDir
    val smaliDir = smaliDir
    val patchesDir = patchesDir
    val projectDir = projectDir

    // No input defined otherwise it could cause a dependency on disassembling
    outputs.dir(patchesDir)

    val diffBin = localProperties.getProperty("diff.bin", null)
        ?: project.findProperty("diff.bin") as String?
        ?: "diff"

    doLast {
        if (!smaliDir.exists() || !smaliOriginalDir.get().asFile.exists()) {
            error("Smali directory does not exist! Run the disassembleWithPatches task")
        }

        val (result, output) = execOperations.execIgnoreCode(logger) {
            executable = diffBin
            args = listOf(
                "--unified",
                "--minimal",
                "--new-file",
                "--recursive",
                "--strip-trailing-cr",
                "--show-function-line=.method",
                "./" + smaliOriginalDir.get().asFile
                    .toRelativeString(projectDir)
                    .replace('\\', '/'),
                "./" + smaliDir
                    .toRelativeString(projectDir)
                    .replace('\\', '/'),
            )
        }

        // diff returns 1 if changes are present
        if (result.exitValue !in 0..1) result.assertNormalExitValue()

        val diffs = output
            .replace("\r\n", "\n") // Replace CRLF endings with LF
            .split("^diff --unified.+?\\R".toRegex(RegexOption.MULTILINE)) // Split by file diff header
            .filter(String::isNotBlank)

        patchesDir.deleteRecursively()
        patchesDir.mkdirs()
        patchesDir.resolve(".gitkeep").createNewFile()

        val classNameRegex = """^\+\+\+ \.?[\/]?smali[\/](.+?)\.smali\t""".toRegex(RegexOption.MULTILINE)
        for (diff in diffs) {
            val (className) = classNameRegex.find(diff)?.destructured
                ?: error("failed to parse diff:\n$diff")

            logger.lifecycle("Writing patch for class $className")

            val cleanDiff = diff
                .split("\n")
                .toMutableList()
                .also {
                    it[0] = "--- smali_original/$className.smali"
                    it[1] = "+++ smali/$className.smali"
                }
                .joinToString("\n")

            File(patchesDir, "$className.patch")
                .apply { parentFile.mkdirs() }
                .writeText(cleanDiff)
        }
    }
}
