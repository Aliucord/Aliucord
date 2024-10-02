import com.android.build.gradle.LibraryExtension
import java.io.ByteArrayOutputStream

version = "1.0.0"

// Make dependency configuration for build tools
val buildTools: Configuration by configurations.creating

repositories {
    mavenCentral()
    google()
}

dependencies {
    val smaliVersion = "3.0.7"
    buildTools("com.android.tools.smali:smali:$smaliVersion")
    buildTools("com.android.tools.smali:smali-baksmali:$smaliVersion")
}

val patchesDir = projectDir.resolve("src")
val smaliDir = projectDir.resolve("smali")
val smaliOriginalDir = buildDir.resolve("smali_original")
val discordApk = project.gradle.gradleUserHomeDir
    .resolve("caches/aliucord/discord/discord-${libs.discord.get().version}.apk")

// --- Public tasks --- //

task<Zip>("package") {
    group = "aliucord"
    archiveFileName.set("patches.zip")
    destinationDirectory.set(buildDir)
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false

    from(patchesDir)
    include(".gitkeep")
    include("**/*.patch")
}

task("deployWithAdb") {
    group = "aliucord"
    dependsOn("package")

    val patchesPath = buildDir.resolve("patches.zip").absolutePath
    val remotePatchesDir = "/storage/emulated/0/Android/data/com.aliucord.manager/cache/patches"

    doLast {
        val android = project(":Aliucord").extensions
            .getByName<LibraryExtension>("android")

        exec { commandLine(android.adbExecutable, "shell", "mkdir", "-p", remotePatchesDir) }
        exec { commandLine(android.adbExecutable, "push", patchesPath, "$remotePatchesDir/$version.custom.zip") }
    }
}

task("disassembleWithPatches") {
    group = "aliucord"
    dependsOn("disassembleInternal", "copyDisassembled", "applyPatches")
}

task("test") {
    group = "aliucord"
    dependsOn("assemble")
}

task("writePatches") {
    group = "aliucord"
    mustRunAfter("applyPatches")

    doFirst {
        if (!smaliDir.exists() || !smaliOriginalDir.exists()) {
            error("Smali directory does not exist! Run the disassembleWithPatches task")
        }

        val stdout = ByteArrayOutputStream()
        val result = exec {
            isIgnoreExitValue = true
            standardOutput = stdout
            errorOutput = System.err
            executable = "diff"
            args = listOf(
                "--unified",
                "--minimal",
                "--new-file",
                "--recursive",
                "--strip-trailing-cr",
                "--show-function-line=.method",
                smaliOriginalDir.toRelativeString(projectDir),
                smaliDir.toRelativeString(projectDir),
            )
        }

        // diff returns 1 if changes are present
        if (result.exitValue !in 0..1) result.assertNormalExitValue()

        val diffs = stdout
            .toString() // Convert bytes to string
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

            File(patchesDir, "$className.patch")
                .apply { parentFile.mkdirs() }
                .writeText(diff)
        }
    }
}

task<Delete>("clean") {
    delete(buildDir)
    delete(smaliDir)
}

// --- Internal tasks --- //

task<JavaExec>("disassembleInternal") {
    classpath = buildTools
    jvmArgs = listOf("-Xmx2G")
    standardOutput = System.out
    errorOutput = System.err

    outputs.upToDateWhen {
        smaliOriginalDir.exists()
    }

    mainClass.set("com.android.tools.smali.baksmali.Main")
    args = listOf(
        "disassemble",
        "--use-locals",
        "--output", smaliOriginalDir.absolutePath,
        discordApk.absolutePath,
    )
}

task<Copy>("copyDisassembled") {
    mustRunAfter("disassembleInternal")
    doFirst {
        delete(smaliDir)
    }

    from(smaliOriginalDir)
    destinationDir = smaliDir
}

task("applyPatches") {
    mustRunAfter("copyDisassembled")
    doLast {
        val patchFiles = fileTree(patchesDir).filter { it.name.endsWith(".patch") }

        for (patchFile in patchFiles) {
            val className = patchFile
                .toRelativeString(patchesDir)
                .removeSuffix(".patch")

            logger.lifecycle("Applying smali patches to class $className")

            exec {
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

task<JavaExec>("assemble") {
    val outputDex = buildDir.resolve("patched.dex").absolutePath

    standardOutput = System.out
    errorOutput = System.err
    classpath = buildTools
    jvmArgs = listOf("-Xmx2G")
    mainClass.set("com.android.tools.smali.smali.Main")
    args = listOf(
        "assemble",
        "--verbose",
        "--output", outputDex,
        smaliDir.absolutePath,
    )

    mustRunAfter("applyPatches")
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
