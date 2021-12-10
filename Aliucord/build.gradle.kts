plugins {
    id("com.android.library")
    id("maven-publish")
    id("com.aliucord.gradle")
    id("kotlin-android")
    id("org.jetbrains.dokka")
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
        buildConfigField("int", "DISCORD_VERSION", findProperty("discord_version") as String)
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
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs +
            "-Xno-call-assertions" +
            "-Xno-param-assertions" +
            "-Xno-receiver-assertions"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.0.5"
    }
}


dependencies {
    api("androidx.appcompat:appcompat:1.3.1")
    api("com.google.android.material:material:1.4.0")
    api("androidx.constraintlayout:constraintlayout:2.1.1")

    api("androidx.compose.ui:ui:1.0.5")
    api("androidx.compose.ui:ui-tooling:1.0.5")
    api("androidx.compose.foundation:foundation:1.0.5")
    api("androidx.compose.material:material:1.0.5")

    discord("com.discord:discord:${findProperty("discord_version")}")
    api(files("../.assets/pine.jar"))
}

tasks.dokkaHtml.configure {
    dokkaSourceSets {
        named("main") {
            noAndroidSdkLink.set(false)
            includeNonPublic.set(false)
        }
    }
}

tasks.dokkaJavadoc.configure {
    dokkaSourceSets {
        named("main") {
            noAndroidSdkLink.set(false)
            includeNonPublic.set(false)
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
