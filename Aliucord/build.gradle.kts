@file:Suppress("UnstableApiUsage")

plugins {
    id("maven-publish")
    id("org.jetbrains.dokka")
}

group = "com.aliucord"
version = "2.0.1"

aliucord {
    projectType.set(com.aliucord.gradle.ProjectType.CORE)
}

android {
    defaultConfig {
        buildConfigField("String", "VERSION", "\"$version\"")
        buildConfigField("boolean", "RELEASE", System.getenv("RELEASE") ?: "false")
        buildConfigField("int", "DISCORD_VERSION", libs.versions.discord.get())
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    api(libs.appcompat)
    api(libs.material)
    api(libs.constraintlayout)
    api(libs.aliuhook)
}

tasks {
    dokkaHtml.configure {
        dokkaSourceSets {
            named("main") {
                noAndroidSdkLink.set(false)
                includeNonPublic.set(false)
            }
        }
    }

    dokkaJavadoc.configure {
        dokkaSourceSets {
            named("main") {
                noAndroidSdkLink.set(false)
                includeNonPublic.set(false)
            }
        }
    }

    create("pushDebuggable") {
        group = "aliucord"

        val aliucordPath = "/storage/emulated/0/Aliucord/"

        doLast {
            exec {
                commandLine(android.adbExecutable, "shell", "touch", "$aliucordPath.debuggable")
            }

            exec {
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
