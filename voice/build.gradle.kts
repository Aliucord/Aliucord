plugins {
    alias(libs.plugins.aliucord.injector)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    id("maven-publish")
}

android {
    namespace = "com.aliucord.voice"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        buildConfigField("String", "VERSION", "\"90.0.29-krisp_vad_overuse\"")
        buildConfigField("String", "LIBDISCORD_BASE", "\"333.12\"")
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        buildConfig = true
    }

    publishing {
        multipleVariants {
            allVariants()
        }
    }
}

dependencies {
    // Must be declared as compileOnly before Discord to override classes
    @Suppress("AvoidDuplicateDependencies")
    compileOnly(libs.webrtc)

    compileOnly(libs.discord)
    compileOnly(libs.kotlin.stdlib)

    // Include as a bundled dependency for Injector
    @Suppress("AvoidDuplicateDependencies")
    implementation(libs.webrtc)
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
