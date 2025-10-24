version = "2.3.1"

plugins {
    alias(libs.plugins.aliucord.injector)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
}

android {
    namespace = "com.aliucord"
    compileSdk = 36

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
        )
    }
}

dependencies {
    compileOnly(libs.aliuhook)
    compileOnly(libs.appcompat)
    compileOnly(libs.discord)
    compileOnly(libs.kotlin.stdlib)
}
