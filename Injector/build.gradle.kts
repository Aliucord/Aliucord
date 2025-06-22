@file:Suppress("UnstableApiUsage")

version = "2.1.1"

aliucord {
    projectType = com.aliucord.gradle.ProjectType.INJECTOR
}

android {
    buildFeatures {
        buildConfig = false
        androidResources = false
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.aliuhook)
}
