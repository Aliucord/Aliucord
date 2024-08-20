pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        maven("https://jitpack.io")
        maven("https://maven.aliucord.com/snapshots")
    }
}

include(":Aliucord")
include(":Injector")
include(":patches")
rootProject.name = "Aliucord"
