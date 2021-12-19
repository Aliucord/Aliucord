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

group = "com.aliucord"

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
        viewBinding = true
    }
}


dependencies {
    api("androidx.appcompat:appcompat:1.3.1")
    api("com.google.android.material:material:1.4.0")
    api("androidx.constraintlayout:constraintlayout:2.1.1")

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
                from(components["debug"])
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
                    setUrl("https://maven.aliucord.com/")
                }
            } else {
                mavenLocal()
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
