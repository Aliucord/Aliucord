@file:Suppress("UnstableApiUsage")

plugins {
    `maven-publish`
    alias(libs.plugins.aliucord.core)
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin)
}

group = "com.aliucord"
version = "2.2.1"

android {
    namespace = "com.aliucord"
    compileSdkVersion(36)

    defaultConfig {
        minSdk = 24
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
        }
    }

    defaultConfig {
        buildConfigField("String", "VERSION", "\"$version\"")
        buildConfigField("boolean", "RELEASE", System.getenv("RELEASE") ?: "false")
        buildConfigField("int", "DISCORD_VERSION", libs.versions.discord.get())
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    publishing {
        singleVariant("debug") {}
    }
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

dependencies {
    val discord by configurations

    discord(libs.discord)
    compileOnly(libs.aliuhook)
    compileOnly(libs.appcompat)
    compileOnly(libs.constraintlayout)
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.material)
    compileOnly(project(":Injector")) // Needed to access certain stubs
}

tasks {
    register("pushDebuggable") {
        group = "aliucord"

        val aliucordPath = "/storage/emulated/0/Aliucord/"

        doLast {
            providers.exec {
                commandLine(android.adbExecutable, "shell", "touch", "$aliucordPath.debuggable")
            }

            providers.exec {
                commandLine(
                    android.adbExecutable,
                    "push",
                    rootProject.file(".assets/AndroidManifest-debuggable.xml"),
                    "${aliucordPath}AndroidManifest.xml"
                )
            }
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>(project.name) {
                from(components["debug"])
                artifact(tasks["debugSourcesJar"])
            }
        }

        repositories {
            val username = System.getenv("MAVEN_USERNAME")
            val password = System.getenv("MAVEN_PASSWORD")

            if (username != null && password != null) {
                maven {
                    credentials {
                        this.username = username
                        this.password = password
                    }
                    setUrl("https://maven.aliucord.com/snapshots")
                }
            } else {
                mavenLocal()
            }
        }
    }
}
