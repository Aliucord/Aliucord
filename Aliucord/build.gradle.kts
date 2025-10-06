@file:Suppress("UnstableApiUsage")

plugins {
    id("maven-publish")
    id("org.jetbrains.dokka")
}

group = "com.aliucord"
version = "2.5.0"

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
    compileOnly(project(":Injector")) // Needed to access certain stubs
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
