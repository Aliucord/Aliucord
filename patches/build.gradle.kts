import com.android.build.gradle.LibraryExtension
import java.io.ByteArrayOutputStream

version = "1.2.0"

// Make dependency configuration for build tools
val buildTools: Configuration by configurations.creating

repositories {
    mavenCentral()
    google()
}

dependencies {
    buildTools(libs.smali)
    buildTools(libs.smali.baksmali)
}

val patchesDir = projectDir.resolve("src")
val smaliDir = projectDir.resolve("smali")
val smaliOriginalDir = layout.buildDirectory.file("smali_original").get().asFile
val discordApk = project.gradle.gradleUserHomeDir
    .resolve("caches/aliucord/discord/discord-${libs.discord.get().version}.apk")

// --- Public tasks --- //
val packageTask = tasks.register<Zip>("package") {
    group = "aliucord"
    archiveFileName = "patches.zip"
    destinationDirectory = layout.buildDirectory
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false

    from(patchesDir)
    include(".gitkeep")
    include("**/*.patch")
}

tasks.register("deployWithAdb") {
    group = "aliucord"
    dependsOn(packageTask)

    val patchesPath = layout.buildDirectory.files("patches.zip").asPath
    val remotePatchesDir = "/storage/emulated/0/Android/data/com.aliucord.manager/cache/patches"

    doLast {
        val android = project(":Aliucord").extensions
            .getByName<LibraryExtension>("android")

        providers.exec { commandLine(android.adbExecutable, "shell", "mkdir", "-p", remotePatchesDir) }.result.get()
        providers.exec { commandLine(android.adbExecutable, "push", patchesPath, "$remotePatchesDir/$version.custom.zip") }.result.get()
    }
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

    standardOutput = System.out
    errorOutput = System.err
    classpath = buildTools
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

        val stdout = ByteArrayOutputStream()
        val result = providers.exec {
            isIgnoreExitValue = true
            standardOutput = stdout
            errorOutput = System.err
            workingDir = projectDir
            executable = "diff"
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
        }.result.get()

        // diff returns 1 if changes are present
        if (result.exitValue !in 0..1) result.assertNormalExitValue()

        val diffs = stdout
            .toString() // Convert bytes to string
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

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
    delete(smaliDir)
}

// --- Internal tasks --- //

val disassembleInternal by tasks.registering(JavaExec::class) {
    classpath = buildTools
    jvmArgs = listOf("-Xmx2G")
    standardOutput = System.out
    errorOutput = System.err

    outputs.upToDateWhen {
        smaliOriginalDir.exists()
    }

    doFirst {
        zipTree(discordApk.absolutePath).matching { include("classes*.dex") }.visit {
            logger.lifecycle("Disassembling $name")
            providers.javaexec {
                classpath = buildTools
                jvmArgs = listOf("-Xmx2G")
                standardOutput = System.out
                errorOutput = System.err

                systemProperty("line.separator", "\n") // Ensure smali output uses LF endings
                mainClass.set("com.android.tools.smali.baksmali.Main")
                args = listOf(
                    "disassemble",
                    "--use-locals",
                    "--output", smaliOriginalDir.absolutePath,
                    "${discordApk.absolutePath}/$name",
                )
            }
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

            providers.exec {
                standardOutput = System.out
                errorOutput = System.err
                executable = "patch"
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
