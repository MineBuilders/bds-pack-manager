import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.konan.target.Architecture

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    mingwX64 {
        binaries.executable {
            windowsResources(projectDir.resolve("src/mingwMain/resources/manager.rc"))
            linkerOpts("-Wl,/SUBSYSTEM:WINDOWS")
        }
    }

    compilerOptions.optIn.addAll(
        "kotlinx.cinterop.ExperimentalForeignApi",
    )

    sourceSets.mingwMain.dependencies {
        implementation(files(projectDir.resolve("libs/libui-windows64.klib")))
        implementation(files(projectDir.resolve("libs/libui-ktx-windows64.klib")))
        implementation(projects.core)
    }
}

// https://github.com/IgnatBeresnev/injector4k/blob/3f9f63e988a2720ba29745239e43a640098e9e67/gui/build.gradle.kts#L40
@Suppress("SpellCheckingInspection")
fun Executable.windowsResources(rcFile: File) {
    val taskName = linkTaskName.replaceFirst("link", "windres")
    val outFile = layout.buildDirectory.file("processedResources/$taskName.res").get().asFile

    val windresTask = tasks.create<Exec>(taskName) {
        val konanDataDir = System.getenv("KONAN_DATA_DIR") ?: "${System.getProperty("user.home")}/.konan"
        val toolchainBinDir = when (target.konanTarget.architecture) {
            Architecture.X86 -> File("$konanDataDir/dependencies/msys2-mingw-w64-i686-2/bin").invariantSeparatorsPath
            Architecture.X64 -> File("$konanDataDir/dependencies/msys2-mingw-w64-x86_64-2/bin").invariantSeparatorsPath
            else -> error("Unsupported architecture")
        }

        inputs.file(rcFile)
        outputs.file(outFile)
        commandLine("$toolchainBinDir/windres", rcFile, "-D_${buildType.name}", "-O", "coff", "-o", outFile)
        environment("PATH", "$toolchainBinDir;${System.getenv("PATH")}")

        dependsOn(compilation.compileTaskProvider.get())
    }

    linkTaskProvider.get().dependsOn(windresTask)
    linkerOpts(outFile.toString())
}
