import fs.Directory
import fs.File
import fs.showPicker
import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
import platform.windows.*

fun main() = runBlocking {
    openManager(getSelfDirectory())
}

suspend fun openManager(root: Directory) {
    if (root.resolveFileName("server.properties") != null)
        return appWindow(PackManager(root))
    if (MessageBoxW(
        null,
        "当前启动目录非 BDS 目录，请重新指定目录！",
        "警告",
        (MB_OKCANCEL or MB_ICONWARNING).convert()
    ) == IDOK) openManager(Directory.showPicker())
}

fun getSelfDirectory() = memScoped {
    val buffer = allocArray<WCHARVar>(MAX_PATH)
    if (GetModuleFileNameW(null, buffer, MAX_PATH.convert()) == 0u)
        error("Failed to get self path: ${GetLastError()}")
    File(buffer.toKStringFromUtf16()).parent!!
}
