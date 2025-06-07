import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCPointer
import libui.ktx.label
import libui.ktx.page
import libui.ktx.tabpane
import libui.ktx.vbox
import platform.windows.*

fun appWindow(manager: PackManager) = libui.ktx.appWindow(
    title = "BDS 资源管理器",
    width = 600,
    height = 800,
    margined = false,
) {
    memScoped {
        val screenWidth = GetSystemMetrics(SM_CXSCREEN)
        val screenHeight = GetSystemMetrics(SM_CYSCREEN)
        val x = (screenWidth - contentSize.width) / 2
        val y = (screenHeight - contentSize.height) / 2
        val hwnd = getHandle().toLong().toCPointer<HWND__>()
        SetWindowPos(hwnd, null, x, y, 0, 0, (SWP_NOSIZE or SWP_NOZORDER).convert())
    }
    tabpane {
        page("总览") {
            vbox { label("qwq") }
        }
        page("全局包") {
            vbox { label("awa") }
        }
        page("主世界") {
            vbox { label("ovo") }
        }
    }
}
