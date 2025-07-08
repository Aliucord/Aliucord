@file:Suppress("UnstableApiUsage")

version = "2.1.2"

aliucord {
    projectType.set(com.aliucord.gradle.ProjectType.INJECTOR)
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
