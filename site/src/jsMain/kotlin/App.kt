import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import bds_pack_manager.site.generated.resources.*
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import ui.component.MenuNode
import ui.component.NestedDropdownMenu
import ui.theme.Contrast
import ui.utils.toDp

@Composable
fun App(switchTheme: ThemeSwitcher) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val state = rememberScrollState()
            Column(
                Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(state)
            ) {
                Spacer(Modifier.height(window.innerHeight.toDp() * .4f))
                Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.displayLarge)
                Text(
                    stringResource(Res.string.app_description),
                    modifier = Modifier
                        .padding(start = 3.dp, top = 2.dp)
                        .alpha(0.8f),
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(Modifier.height(16.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch { snackbarHostState.showSnackbar(getString(Res.string.not_available)) }
                        },
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(
                            Icons.Rounded.FileOpen,
                            contentDescription = "Localized description",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(Res.string.main_open_directory))
                    }
                    FilledTonalButton(onClick = {
                        scope.launch { snackbarHostState.showSnackbar(getString(Res.string.not_available)) }
                    }) {
                        Text(stringResource(Res.string.main_open_directory_remotely))
                    }
                }
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(state),
                modifier = Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                style = defaultScrollbarStyle().copy(
                    unhoverColor = MaterialTheme.colorScheme.primary,
                    hoverColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )

            AppSettings(switchTheme)
        }
    }
}

@Composable
fun BoxScope.AppSettings(switchTheme: ThemeSwitcher) {
    var settingsExpanded by remember { mutableStateOf(false) }
    IconButton(
        onClick = { settingsExpanded = true },
        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
    ) {
        Icon(Icons.Rounded.Settings, contentDescription = stringResource(Res.string.main_settings))
    }
    NestedDropdownMenu(
        expanded = settingsExpanded,
        onDismissRequest = { settingsExpanded = false },
        offset = DpOffset(16.dp, 0.dp),
    ) {
        +MenuNode(stringResource(Res.string.main_settings_theme), leadingIcon = Icons.Rounded.Palette) {
            val (isDarkTheme, themeContrast) = switchTheme(null, null)
            +MenuNode(
                stringResource(Res.string.main_settings_theme_dark),
                trailingIcon = if (isDarkTheme) Icons.Rounded.CheckBox else Icons.Rounded.CheckBoxOutlineBlank,
                trailingFunctional = true,
                onClick = { switchTheme(!isDarkTheme, null) },
            )
            +MenuNode(stringResource(Res.string.main_settings_theme_contrast)) {
                for ((contrast, text) in arrayOf(
                    Contrast.Normal to Res.string.main_settings_theme_contrast_normal,
                    Contrast.Medium to Res.string.main_settings_theme_contrast_medium,
                    Contrast.High to Res.string.main_settings_theme_contrast_high,
                )) +MenuNode(
                    stringResource(text),
                    trailingIcon = if (themeContrast == contrast) Icons.Rounded.Check else null,
                    trailingFunctional = true,
                    onClick = { switchTheme(null, contrast) },
                )
            }
        }
        divider()

        var openAboutDialog by remember { mutableStateOf(false) }
        +MenuNode(stringResource(Res.string.main_settings_about), leadingIcon = Icons.Rounded.Info, onClick = {
            openAboutDialog = true
        })
        if (openAboutDialog) AlertDialog(
            icon = { Icon(Icons.Rounded.Info, contentDescription = "Example Icon") },
            title = { Text(stringResource(Res.string.main_settings_about)) },
            text = { Text("https://github.com/MineBuilders/bds-pack-manager") },
            onDismissRequest = { openAboutDialog = false },
            confirmButton = {
                TextButton(
                    onClick = { window.open("https://github.com/MineBuilders/bds-pack-manager") }
                ) { Text("Github") }
            },
        )
    }
}
