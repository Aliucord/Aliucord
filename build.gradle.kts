buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.2")
        classpath("com.github.Aliucord:gradle:main-SNAPSHOT")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.5.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://dl.bintray.com/canyie/pine")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
