@file:Suppress("UnstableApiUsage")

version = "2.2.0"

aliucord {
    projectType = com.aliucord.gradle.ProjectType.INJECTOR
}

android {
    androidResources {
        enable = false
    }
    buildFeatures {
        buildConfig = false
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.aliuhook)
}
