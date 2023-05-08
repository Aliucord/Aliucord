@file:Suppress("UnstableApiUsage")

import com.aliucord.gradle.AliucordExtension
import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.aliucord.gradle) apply false
    alias(libs.plugins.dokka) apply false
}

subprojects {
    apply {
        plugin("com.android.library")
        plugin("kotlin-android")
        plugin("com.aliucord.gradle")
    }

    repositories {
        google()
        mavenCentral()
        maven("https://maven.aliucord.com/snapshots")
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

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }

    dependencies {
        val discord by configurations

        discord(rootProject.libs.discord)
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = freeCompilerArgs +
                "-Xno-call-assertions" +
                "-Xno-param-assertions" +
                "-Xno-receiver-assertions"
        }
    }
}

fun Project.android(configuration: BaseExtension.() -> Unit) =
    extensions.getByName<BaseExtension>("android").configuration()

fun Project.aliucord(configuration: AliucordExtension.() -> Unit) =
    extensions.getByName<AliucordExtension>("aliucord").configuration()
