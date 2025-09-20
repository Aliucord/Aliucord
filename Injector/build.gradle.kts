@file:Suppress("UnstableApiUsage")

version = "2.1.2"

plugins {
    alias(libs.plugins.aliucord.injector)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
}

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

    androidResources {
        enable = false
    }

    buildFeatures {
        buildConfig = false
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
    compileOnly(libs.kotlin.stdlib)
}
