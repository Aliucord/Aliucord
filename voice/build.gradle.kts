import java.io.File
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import org.gradle.api.file.FileCollection
import java.util.zip.ZipFile

plugins {
    alias(libs.plugins.aliucord.injector)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    id("maven-publish")
}

android {
    namespace = "com.aliucord.aliuvoice"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    publishing {
        singleVariant("release") {}
    }
}

val webrtcClasses by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

dependencies {
    compileOnly(libs.webrtc)
    compileOnly(libs.discord)
    compileOnly(libs.kotlin.stdlib)
    webrtcClasses(libs.webrtc)
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xno-call-assertions",
            "-Xno-param-assertions",
            "-Xno-receiver-assertions",
        )
    }
}

val injectWebrtcDex by tasks.registering {
    dependsOn("bundleReleaseAar", "make")

    val aar = layout.buildDirectory.file("outputs/aar/${project.name}-release.aar").get().asFile
    val work = layout.buildDirectory.dir("aliuvoice").get().asFile
    val sdk = android.sdkDirectory
    val compileSdkVersion = android.compileSdk ?: error("compileSdk not set")
    val isWindows = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)
    val webrtcArtifacts: FileCollection = webrtcClasses

    doLast {
        fun extractClassesJar(zip: File, dest: File) {
            ZipFile(zip).use { zf ->
                val entry = zf.getEntry("classes.jar") ?: error("classes.jar not found in ${zip.name}")
                dest.parentFile.mkdirs()
                zf.getInputStream(entry).use { input -> dest.outputStream().use { input.copyTo(it) } }
            }
        }

        fun runCmd(vararg cmd: String) {
            val proc = ProcessBuilder(*cmd).redirectErrorStream(true).start()
            proc.inputStream.bufferedReader().forEachLine { println(it) }
            if (proc.waitFor() != 0) error("Command failed: ${cmd.joinToString(" ")}")
        }

        work.deleteRecursively()
        work.mkdirs()

        val classesJar = File(work, "classes.jar")
        extractClassesJar(aar, classesJar)
        require(classesJar.exists()) { "classes.jar missing in AAR" }

        val webrtcArtifact = webrtcArtifacts.singleFile
        val webrtcJar = File(work, "webrtc-classes.jar")
        if (webrtcArtifact.name.endsWith(".aar")) {
            extractClassesJar(webrtcArtifact, webrtcJar)
        } else {
            webrtcArtifact.copyTo(webrtcJar, overwrite = true)
        }
        require(webrtcJar.exists()) { "webrtc classes missing in ${webrtcArtifact.name}" }

        val buildToolsVersion = "36.0.0"
        val buildToolsRoot = File(sdk, "build-tools")
        val buildTools = File(buildToolsRoot, buildToolsVersion).takeIf { it.isDirectory }
            ?: buildToolsRoot.listFiles()?.filter { it.isDirectory }?.maxByOrNull { it.name }
            ?: error("No build-tools installed in $sdk")
        val d8 = File(buildTools, if (isWindows) "d8.bat" else "d8")
        val androidJar = File(sdk, "platforms/android-$compileSdkVersion/android.jar")

        runCmd(
            d8.absolutePath,
            "--release",
            "--min-api", "24",
            "--lib", androidJar.absolutePath,
            "--output", work.absolutePath,
            classesJar.absolutePath,
            webrtcJar.absolutePath,
        )

        val dexes = work.listFiles { f -> f.isFile && f.name.endsWith(".dex") }
            ?.sortedBy { it.name }
            ?: error("d8 produced no dex output")

        FileSystems.newFileSystem(URI("jar:${aar.toURI()}"), mapOf("create" to "false")).use { fs ->
            for (dex in dexes) {
                val entryName = if (dex.name == "classes.dex") "webrtc.dex" else dex.name
                Files.copy(dex.toPath(), fs.getPath("/$entryName"), StandardCopyOption.REPLACE_EXISTING)
            }
        }
        println("Injected ${dexes.size} dex file(s) into ${aar.name}")
    }
}

tasks.matching {
    it.name.startsWith("publish") || it.name.startsWith("generateMetadataFile")
}.configureEach {
    dependsOn(injectWebrtcDex)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.aliucord"
                artifactId = "Aliuvoice"
                version = "1.0.0"

                from(components["release"])
            }
        }

        repositories {
            maven {
                url = uri("https://maven.aliucord.com/releases")
                credentials {
                    username = System.getenv("MAVEN_RELEASE_USERNAME")
                    password = System.getenv("MAVEN_RELEASE_PASSWORD")
                }
            }
        }
    }
}
