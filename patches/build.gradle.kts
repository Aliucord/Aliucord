import com.aliucord.gradle.task.adb.DeployComponentTask
import java.util.Properties

version = "1.3.0"

// --- Android --- //

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.aliucord.patches"
    compileSdk = 36
}

// --- Dependencies --- //

// Make dependency configuration for build tools
val smaliTools: Configuration by configurations.creating
val discord: Configuration by configurations.creating

dependencies {
    smaliTools(libs.smali)
    smaliTools(libs.smali.baksmali)
    discord(libs.discord)
}

// --- Other --- //
val localProperties = Properties().apply {
    val file = project.rootProject.file("local.properties")
    if (file.exists()) {
        load(file.inputStream())
    }
}

// --- Shared files --- //

val patchesDir = projectDir.resolve("src")
val smaliDir = projectDir.resolve("smali")
val smaliOriginalDir = layout.buildDirectory.file("smali_original").get().asFile
val patchesBundle = layout.buildDirectory.file("patches.zip").get().asFile

// --- Public tasks --- //

val packageTask by tasks.registering(Zip::class) {
    group = "aliucord"
    archiveFileName = "patches.zip"
    destinationDirectory = layout.buildDirectory
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false

    from(patchesDir)
    include(".gitkeep")
    include("**/*.patch")
}

tasks.register<DeployComponentTask>("deployWithAdb") {
    group = "aliucord"
    componentType = "patches"
    componentVersion = project.version.toString()
    componentFile.set(patchesBundle)
    dependsOn(packageTask)
}

tasks.register("disassembleWithPatches") {
    group = "aliucord"
    dependsOn("disassembleInternal", "copyDisassembled", "applyPatches")
}

tasks.register<JavaExec>("testPatches") {
    group = "aliucord"
    mustRunAfter("applyPatches")

    val outputDex = layout.buildDirectory.file("patched.dex").get().asFile.absolutePath
    val patchFiles = fileTree(patchesDir) { include("**/*.patch") }.files
    val smaliFiles = patchFiles.map { path ->
        path.toRelativeString(patchesDir)
            .replace(".patch", ".smali")
            .let(smaliDir::resolve)
            .absolutePath
    }

    classpath = smaliTools
    jvmArgs = listOf("-Xmx2G")
    mainClass = "com.android.tools.smali.smali.Main"
    args = listOf(
        "assemble",
        "--verbose",
        "--output", outputDex,
    ) + smaliFiles

    doFirst {
        delete(outputDex)

        if (!smaliDir.exists()) {
            error("Smali directory does not exist! Run the disassembleWithPatches task")
        }
    }

    doLast {
        logger.lifecycle("Successfully reassembled dex: {}", outputDex)
    }
}

tasks.register("writePatches") {
    group = "aliucord"
    mustRunAfter("applyPatches")

    doFirst {
        if (!smaliDir.exists() || !smaliOriginalDir.exists()) {
            error("Smali directory does not exist! Run the disassembleWithPatches task")
        }

        val diffBin = localProperties.getProperty("diff.bin", null)
            ?: project.findProperty("diff.bin") as String?
            ?: "diff"

        val output = providers.execIgnoreCode {
            executable = diffBin
            args = listOf(
                "--unified",
                "--minimal",
                "--new-file",
                "--recursive",
                "--strip-trailing-cr",
                "--show-function-line=.method",
                "./" + smaliOriginalDir.toRelativeString(projectDir).replace('\\', '/'),
                "./" + smaliDir.toRelativeString(projectDir).replace('\\', '/'),
            )
        }
        val result = output.result.get()

        // diff returns 1 if changes are present
        if (result.exitValue !in 0..1) result.assertNormalExitValue()

        val diffs = output.standardOutput.asText.get()
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

tasks.named<Delete>("clean") {
    delete(layout.buildDirectory)
    delete(smaliDir)
}

// --- Internal tasks --- //

val disassembleInternal by tasks.registering {
    outputs.upToDateWhen {
        smaliOriginalDir.exists()
    }

    doFirst {
        // Resolve Discord APK dependency to an apk file
        val discordApk = discord.incoming
            .artifactView { attributes.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "apk") }
            .files.singleFile

        val classesFiles = zipTree(discordApk.absolutePath)
            .matching { include("classes*.dex") }

        classesFiles.visit {
            logger.lifecycle("Disassembling $name")
            providers.javaexec {
                classpath = smaliTools
                jvmArgs = listOf("-Xmx2G")
                systemProperty("line.separator", "\n") // Ensure smali output uses LF endings

                mainClass.set("com.android.tools.smali.baksmali.Main")
                args = listOf(
                    "disassemble",
                    "--use-locals",
                    "--output", smaliOriginalDir.absolutePath,
                    "${discordApk.absolutePath}/$name",
                )
            }.result.get()
        }
    }
}

val copyDisassembled by tasks.registering(Copy::class) {
    mustRunAfter(disassembleInternal)
    doFirst {
        delete(smaliDir)
    }

    from(smaliOriginalDir)
    destinationDir = smaliDir
}

tasks.register("applyPatches") {
    mustRunAfter(copyDisassembled)
    doLast {
        val patchFiles = fileTree(patchesDir).filter { it.name.endsWith(".patch") }

        for (patchFile in patchFiles) {
            val className = patchFile
                .toRelativeString(patchesDir)
                .removeSuffix(".patch")

            logger.lifecycle("Applying smali patches to class $className")

            val patchBin = localProperties.getProperty("patch.bin", null)
                ?: project.findProperty("patch.bin") as String?
                ?: "patch"

            providers.execIgnoreCode {
                executable = patchBin
                args = listOf(
                    "--verbose",
                    "--forward",
                    "--unified",
                    smaliDir.resolve("$className.smali").absolutePath,
                    patchFile.absolutePath,
                )
            }
        }
    }
}

private fun ProviderFactory.execIgnoreCode(block: ExecSpec.() -> Unit): ExecOutput = run {
    val result = exec {
        workingDir = projectDir
        isIgnoreExitValue = true
        block(this)
    }

    val stdout = result.standardOutput.asText.get().trim()
    logger.info(stdout)

    val stderr = result.standardError.asText.get().trim()
    if (stderr.isNotEmpty()) {
        logger.error(stderr)
    }

    result
}
