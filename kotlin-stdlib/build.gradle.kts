import com.aliucord.gradle.SensitiveAliucordApi
import com.aliucord.gradle.task.CompileDexTask

plugins {
    alias(libs.plugins.android.library)
}

// Boilerplate unused config
android {
    namespace = "com.aliucord"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }
}

val dexArtifacts by configurations.registering
val intermediates = project.layout.buildDirectory.dir("intermediates")

dependencies {
    dexArtifacts(libs.kotlin.stdlib)
}

val compileDexTask = tasks.register("compileDex", CompileDexTask::class) {
    group = "aliucord-internal"

    @OptIn(SensitiveAliucordApi::class)
    scanDependencies = false

    outputDir.set(intermediates.map { it.dir("dex") })
    input.from(dexArtifacts.map { configuration ->
        configuration.incoming
            .artifactView {
                attributes.attribute(
                    ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE,
                    ArtifactTypeDefinition.JAR_TYPE)
            }
            .files
    })
}

tasks.register("make", Copy::class.java) {
    group = "aliucord"

    from(compileDexTask.map { it.outputDir.file("classes.dex") })
    into(project.layout.buildDirectory.dir("outputs"))
    rename { "kotlin.dex" }
}
