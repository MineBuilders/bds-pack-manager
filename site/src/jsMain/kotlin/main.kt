import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.CanvasBasedWindow
import bds_pack_manager.site.generated.resources.HarmonyOS_Sans_SC_Regular
import bds_pack_manager.site.generated.resources.Res
import bds_pack_manager.site.generated.resources.app_name
import kotlinx.browser.document
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.preloadFont
import org.jetbrains.skiko.wasm.onWasmReady
import ui.theme.AppTheme
import ui.theme.Contrast

typealias ThemeSwitcher = (dark: Boolean?, contrast: Contrast?) -> Pair<Boolean, Contrast>

@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
fun main() {
    onWasmReady {
        CanvasBasedWindow {
            val fullFont by preloadFont(Res.font.HarmonyOS_Sans_SC_Regular)

            val isSystemInDarkTheme = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(isSystemInDarkTheme) }
            var themeContrast by remember { mutableStateOf(Contrast.Normal) }
            AppTheme(
                darkTheme = isDarkTheme,
                contrast = themeContrast,
                defaultTypography = fullFont == null,
            ) {
                App { dark, contrast ->
                    dark?.let { isDarkTheme = it }
                    contrast?.let { themeContrast = it }
                    isDarkTheme to themeContrast
                }
                AnimatedVisibility(
                    visible = fullFont == null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                            .clickable { }
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
                    }
                }
            }

            LaunchedEffect(Unit) {
                document.head!!.getElementsByTagName("title")
                    .item(0)!!
                    .textContent = getString(Res.string.app_name)
            }
        }
    }
}
