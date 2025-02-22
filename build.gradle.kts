@file:Suppress("UnstableApiUsage")

import com.aliucord.gradle.AliucordExtension
import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.aliucord.gradle) apply false
    alias(libs.plugins.dokka) apply false
}

subprojects {
    if (project.name !in arrayOf("Aliucord", "Injector")) return@subprojects

    apply {
        plugin("com.android.library")
        plugin("kotlin-android")
        plugin("com.aliucord.gradle")
    }

    android {
        namespace = "com.aliucord"

        compileSdkVersion(30)

        @Suppress("ExpiredTargetSdkVersion")
        defaultConfig {
            minSdk = 24
            targetSdk = 30
        }

        buildTypes {
            get("release").isMinifyEnabled = false
        }
    }

    dependencies {
        val discord by configurations

        discord(rootProject.libs.discord)
    }

    extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(17)

        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xno-call-assertions",
                "-Xno-param-assertions",
                "-Xno-receiver-assertions"
            )
        }
    }
}

private fun Project.android(configuration: BaseExtension.() -> Unit) =
    extensions.getByName<BaseExtension>("android").configuration()

private fun Project.aliucord(configuration: AliucordExtension.() -> Unit) =
    extensions.getByName<AliucordExtension>("aliucord").configuration()
