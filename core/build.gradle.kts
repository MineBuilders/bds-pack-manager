plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    js {
        browser()
    }

    mingwX64()

    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
        )
        optIn.addAll(
            "kotlinx.cinterop.ExperimentalForeignApi",
        )
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        jsMain.dependencies {
            implementation(kotlinWrappers.browser)
            implementation(npm("fflate", "0.8.2"))
        }
        mingwMain.dependencies {
            implementation(libs.jonasbroeckmann.kzip.toStringLocation()) {
                // we only need miniz binding :)
                exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-io-core")
            }
        }
    }
}

fun Provider<MinimalExternalModuleDependency>.toStringLocation() =
    get().run { "$group:$name:$version" }
