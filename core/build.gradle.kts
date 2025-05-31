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
        }
    }
}
