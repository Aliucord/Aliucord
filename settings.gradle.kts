pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        maven("https://jitpack.io")
        maven("https://maven.aliucord.com/snapshots")
    }
}

include(":aliucord")
include(":injector")
rootProject.name = "Aliucord"
