@file:Suppress("UnstableApiUsage")

import com.vanniktech.maven.publish.AndroidMultiVariantLibrary

plugins {
    alias(libs.plugins.aliucord.core)
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka.html)
    alias(libs.plugins.dokka.javadoc)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.publish)
}

group = "com.aliucord"
version = "2.5.0"

android {
    namespace = "com.aliucord"
    compileSdk = 36

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

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

    lint {
        disable += "SetTextI18n"
    }
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xno-call-assertions",
            "-Xno-param-assertions",
            "-Xno-receiver-assertions",
            "-Xallow-kotlin-package", // Workaround to adding kotlin.enums.EnumEntries polyfill
        )
    }
}

dependencies {
    compileOnly(libs.aliuhook)
    compileOnly(libs.appcompat)
    compileOnly(libs.constraintlayout)
    compileOnly(libs.discord)
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.material)
    compileOnly(project(":Injector")) // Needed to access certain stubs
    coreLibraryDesugaring(libs.desugar)
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(arrayOf(
        "-Xlint:deprecation",
    ))
}

mavenPublishing {
    coordinates("com.aliucord", "Aliucord")
    configure(AndroidMultiVariantLibrary(
        includedBuildTypeValues = setOf("debug"),
    ))
}

publishing {
    repositories {
        maven {
            name = "aliucord"
            url = uri("https://maven.aliucord.com/releases")
            credentials {
                username = System.getenv("MAVEN_RELEASES_USERNAME")
                password = System.getenv("MAVEN_RELEASES_PASSWORD")
            }
        }
        maven {
            name = "aliucordSnapshots"
            url = uri("https://maven.aliucord.com/snapshots")
            credentials {
                username = System.getenv("MAVEN_SNAPSHOTS_USERNAME")
                password = System.getenv("MAVEN_SNAPSHOTS_PASSWORD")
            }
        }
    }
}
