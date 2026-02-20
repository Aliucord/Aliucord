// Please update the reference RNA version when bumping libdiscord.so!
// Reference: 314.13 - Stable
version = "90.0.19-codec-api.0"

plugins {
    alias(libs.plugins.aliucord.injector)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
}

android {
    namespace = "com.aliucord.voice"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        buildConfigField("String", "VERSION", "\"$version\"")
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
        buildConfig = true
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
    compileOnly(libs.discord)
    compileOnly(libs.kotlin.stdlib)
}
