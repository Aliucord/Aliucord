import java.nio.file.Paths
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

plugins {
    id("com.android.library")
    id("maven-publish")
    id("signing")
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.eclipse.jgit:org.eclipse.jgit:6.3.0.202209071007-r")
    }
}

val androidTargetSdkVersion: Int by rootProject.extra
val androidMinSdkVersion: Int by rootProject.extra
val androidBuildToolsVersion: String by rootProject.extra
val androidCompileSdkVersion: Int by rootProject.extra
val androidNdkVersion: String by rootProject.extra
val androidCmakeVersion: String by rootProject.extra

fun findInPath(executable: String): String? {
    val pathEnv = System.getenv("PATH")
    return pathEnv.split(File.pathSeparator).map { folder ->
        Paths.get("${folder}${File.separator}${executable}${if (org.gradle.internal.os.OperatingSystem.current().isWindows) ".exe" else ""}")
            .toFile()
    }.firstOrNull { path ->
        path.exists()
    }?.absolutePath
}

android {
    compileSdk = androidCompileSdkVersion
    ndkVersion = androidNdkVersion
    buildToolsVersion = androidBuildToolsVersion

    buildFeatures {
        buildConfig = false
        prefabPublishing = true
        androidResources = false
        prefab = true
    }

    packagingOptions {
        jniLibs {
            excludes += "**.so"
        }
    }

    prefab {
        register("lsplant") {
            headers = "src/main/jni/include"
        }
    }

    defaultConfig {
        minSdk = androidMinSdkVersion
        targetSdk = androidTargetSdkVersion
    }

    buildTypes {
        all {
            externalNativeBuild {
                cmake {
                    abiFilters("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
                    val flags = arrayOf(
                        "-Wall",
                        "-Werror",
                        "-Qunused-arguments",
                        "-Wno-gnu-string-literal-operator-template",
                        "-fno-rtti",
                        "-fvisibility=hidden",
                        "-fvisibility-inlines-hidden",
                        "-fno-exceptions",
                        "-fno-stack-protector",
                        "-fomit-frame-pointer",
                        "-Wno-builtin-macro-redefined",
                        "-Wno-c++2b-extensions",
                        "-ffunction-sections",
                        "-fdata-sections",
                        "-Wno-unused-value",
                        "-D__FILE__=__FILE_NAME__",
                        "-Wl,--exclude-libs,ALL",
                    )
                    cppFlags("-std=c++20", *flags)
                    cFlags("-std=c18", *flags)
                    val configFlags = arrayOf(
                        "-Oz",
                        "-DNDEBUG"
                    ).joinToString(" ")
                    arguments(
                        "-DCMAKE_CXX_FLAGS_RELEASE=$configFlags",
                        "-DCMAKE_C_FLAGS_RELEASE=$configFlags",
                        "-DDEBUG_SYMBOLS_PATH=${project.buildDir.absolutePath}/symbols/$name",
                    )
                    findInPath("ccache")?.let {
                        println("Using ccache $it")
                        arguments += "-DANDROID_CCACHE=$it"
                    }
                }
            }
        }
        release {
            externalNativeBuild {
                val flags = arrayOf(
                    "-Wl,--gc-sections",
                    "-flto",
                    "-fno-unwind-tables",
                    "-fno-asynchronous-unwind-tables",
                )
                cmake {
                    cppFlags += flags
                    cFlags += flags
                    arguments += "-DANDROID_STL=c++_shared"
                    arguments += "-DCMAKE_BUILD_TYPE=Release"
                }
            }
        }
        debug {
            externalNativeBuild {
                cmake {
                    arguments += "-DANDROID_STL=c++_shared"
                }
            }
        }
        create("standalone") {
            initWith(getByName("release"))
            externalNativeBuild {
                val flags = arrayOf(
                    "-Wl,--gc-sections",
                    "-flto",
                    "-fno-unwind-tables",
                    "-fno-asynchronous-unwind-tables",
                )
                cmake {
                    cppFlags += flags
                    cFlags += flags
                    arguments += "-DANDROID_STL=none"
                    arguments += "-DCMAKE_BUILD_TYPE=Release"
                    arguments += "-DLSPLANT_STANDALONE=ON"
                }
            }
        }
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = false
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/jni/CMakeLists.txt")
            version = androidCmakeVersion
        }
    }
    namespace = "org.lsposed.lsplant"

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
        singleVariant("standalone") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

val symbolsReleaseTask = tasks.register<Jar>("generateReleaseSymbolsJar") {
    from("${project.buildDir.absolutePath}/symbols/release")
    exclude("**/dex_builder")
    archiveClassifier.set("symbols")
    archiveBaseName.set("release")
}

val symbolsStandaloneTask = tasks.register<Jar>("generateStandaloneSymbolsJar") {
    from("${project.buildDir.absolutePath}/symbols/standalone")
    exclude("**/dex_builder")
    archiveClassifier.set("symbols")
    archiveBaseName.set("standalone")
}

val ver = FileRepositoryBuilder().findGitDir(rootProject.file(".git")).runCatching {
    build().use {
        Git(it).describe().setTags(true).setAbbrev(0).call().removePrefix("v")
    }
}.getOrNull() ?: "0.0"
println("${rootProject.name} version: $ver")

publishing {
    publications {
        fun MavenPublication.setup() {
            group = "org.lsposed.lsplant"
            version = ver
            pom {
                name.set("LSPlant")
                description.set("A hook framework for Android Runtime (ART)")
                url.set("https://github.com/LSPosed/LSPlant")
                licenses {
                    license {
                        name.set("GNU Lesser General Public License v3.0")
                        url.set("https://github.com/LSPosed/LSPlant/blob/master/LICENSE")
                    }
                }
                developers {
                    developer {
                        name.set("Lsposed")
                        url.set("https://lsposed.org")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/LSPosed/LSPlant.git")
                    url.set("https://github.com/LSPosed/LSPlant")
                }
            }
        }
        register<MavenPublication>("lsplant") {
            artifactId = "lsplant"
            afterEvaluate {
                from(components.getByName("release"))
                artifact(symbolsReleaseTask)
            }
            setup()
        }
        register<MavenPublication>("lsplantStandalone") {
            artifactId = "lsplant-standalone"
            afterEvaluate {
                from(components.getByName("standalone"))
                artifact(symbolsStandaloneTask)
            }
            setup()
        }
    }
    repositories {
        maven {
            name = "ossrh"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials(PasswordCredentials::class)
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/LSPosed/LSPlant")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    dependencies {
        "standaloneCompileOnly"("dev.rikka.ndk.thirdparty:cxx:1.2.0")
    }
}

signing {
    val signingKey = findProperty("signingKey") as String?
    val signingPassword = findProperty("signingPassword") as String?
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
    sign(publishing.publications)
}
