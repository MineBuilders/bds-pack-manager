plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    js {
        browser()
    }

    mingwX64()
}
