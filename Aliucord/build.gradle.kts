plugins {
    id("com.android.library")
    id("maven-publish")
    id("com.aliucord.gradle")
    id("kotlin-android")
}

fun getGitHash(): String {
    val stdout = org.apache.commons.io.output.ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", "rev-parse", "--short", "HEAD")
        standardOutput = stdout
        isIgnoreExitValue = true
    }
    return stdout.toString().trim()
}

aliucord {
    projectType.set(com.aliucord.gradle.ProjectType.CORE)
}

android {
    compileSdk = 30

    defaultConfig {
        minSdk = 24
        targetSdk = 30

        buildConfigField("String", "GIT_REVISION", "\"${getGitHash()}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    api("androidx.appcompat:appcompat:1.3.1")
    api("com.google.android.material:material:1.4.0")
    api("androidx.constraintlayout:constraintlayout:2.1.0")

    discord("com.discord:discord:${findProperty("discord_version")}")
    api("com.github.Aliucord:pine:83f67b2cdb")
}

// https://www.stkent.com/2016/06/10/adventures-with-javadocs-part-3.html
android.libraryVariants.all {
    if (name == "release") {
        val variant = this
        task<Javadoc>("javadoc") {
            val compiler = variant.javaCompileProvider.get()
            source = compiler.source
            classpath = files(android.bootClasspath.joinToString(File.pathSeparator))
            classpath += compiler.classpath
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            register(project.name, MavenPublication::class) {
                group = "com.github.Aliucord"

                from(components["debug"])
            }
        }
    }
}

task("pushDebuggable") {
    group = "aliucord"

    val aliucordPath = "/storage/emulated/0/Aliucord/"

    doLast {
        exec {
            commandLine(android.adbExecutable, "shell", "touch", "$aliucordPath.debuggable")
        }

        exec {
            commandLine(android.adbExecutable, "push", rootProject.file(".assets/AndroidManifest-debuggable.xml"), aliucordPath + "AndroidManifest.xml")
        }
    }
}
