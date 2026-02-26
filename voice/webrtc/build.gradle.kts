// Source stamp 2024-12-12T04:05:15
version = "53c76ef"

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
}

android {
    namespace = "org.webrtc"
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
        resValues = false
    }

    androidComponents {
        beforeVariants(selector().withBuildType("release")) { variantBuilder ->
            variantBuilder.enable = false
        }
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
    compileOnly(libs.kotlin.stdlib)
    // Filling in some gaps w.r.t annotations
    compileOnly(libs.appcompat)
}
