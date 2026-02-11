// Reference: 314.13 - Stable
version = "90.0.19-codec-api.0"

plugins {
    alias(libs.plugins.aliucord.core)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
}

android {
    namespace = "com.aliucord.voice"
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

    androidComponents {
        beforeVariants(selector().withBuildType("release")) { variantBuilder ->
            variantBuilder.enable = false
        }
    }

    androidResources {
        enable = false
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
    compileOnly(libs.discord)
    compileOnly(libs.kotlin.stdlib)
    coreLibraryDesugaring(libs.desugar)
}
