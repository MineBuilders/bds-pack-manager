import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import bds_pack_manager.site.generated.resources.*
import fs.Directory
import fs.webdav.WDDirectory
import fs.webdav.WebDavClient
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ui.component.BdsViewer
import ui.component.MenuNode
import ui.component.NestedDropdownMenu
import ui.theme.Contrast
import ui.utils.toDp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(switchTheme: ThemeSwitcher) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var manager by remember { mutableStateOf<PackManager?>(null) }
    remember(manager) {
        if (manager != null) window.addEventListener("beforeunload", callback = {
            it.preventDefault()
            it.asDynamic().returnValue = ""
        })
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
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

                var openRemoteDialog by remember { mutableStateOf(false) }
                Spacer(Modifier.height(16.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val directory = Directory.requestUser()
                                    manager = PackManager(directory)
                                } catch (e: Throwable) {
                                    snackbarHostState.showSnackbar(e.message.toString())
                                }
                            }
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
                        openRemoteDialog = true
                    }) {
                        Text(stringResource(Res.string.main_open_directory_remotely))
                    }
                }

                var remoteHost by remember { mutableStateOf("http://localhost:6065") }
                var remoteUsername by remember { mutableStateOf("admin") }
                var remotePassword by remember { mutableStateOf("admin") }
                var remoteConnecting by remember { mutableStateOf(false) }
                var remoteError by remember { mutableStateOf<String?>(null) }
                remember(remoteHost, remoteUsername, remotePassword) { remoteError = null }
                if (openRemoteDialog) AlertDialog(
                    title = { Text(stringResource(Res.string.main_open_directory_remotely)) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            var width by remember { mutableIntStateOf(0) }
                            val widthDp = width.toDp()
                            AnimatedVisibility(remoteConnecting) {
                                LinearProgressIndicator(Modifier.width(widthDp))
                            }
                            AnimatedVisibility(remoteError != null) {
                                Text(remoteError ?: "", Modifier.width(widthDp))
                            }
                            OutlinedTextField(
                                modifier = Modifier.onGloballyPositioned { width = it.size.width },
                                value = remoteHost,
                                onValueChange = { remoteHost = it },
                                label = { Text(stringResource(Res.string.main_open_directory_remotely_host)) },
                                isError = remoteError != null,
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = remoteUsername,
                                onValueChange = { remoteUsername = it },
                                label = { Text(stringResource(Res.string.main_open_directory_remotely_username)) },
                                isError = remoteError != null,
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = remotePassword,
                                onValueChange = { remotePassword = it },
                                label = { Text(stringResource(Res.string.main_open_directory_remotely_password)) },
                                isError = remoteError != null,
                                singleLine = true,
                            )
                        }
                    },
                    onDismissRequest = { openRemoteDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        remoteError = null
                                        remoteConnecting = true
                                        val client = WebDavClient(
                                            host = remoteHost,
                                            username = remoteUsername,
                                            password = remotePassword
                                        )
                                        client.ping()
                                        val directory = WDDirectory(client)
                                        manager = PackManager(directory)
                                        openRemoteDialog = false
                                        remoteConnecting = false
                                    } catch (e: Throwable) {
                                        val message = e.message.toString()
                                        remoteError = message
                                        remoteConnecting = false
                                        snackbarHostState.showSnackbar(message)
                                    }
                                }
                            }
                        ) {
                            Text(stringResource(Res.string.main_open_directory_remotely_connect))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { openRemoteDialog = false }
                        ) {
                            Text(stringResource(Res.string.main_open_directory_remotely_cancel))
                        }
                    },
                )

                AnimatedVisibility(manager != null, Modifier.fillMaxWidth()) {
                    BdsViewer(Modifier.padding(top = 32.dp, bottom = 80.dp), manager!!)
                }
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState),
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
