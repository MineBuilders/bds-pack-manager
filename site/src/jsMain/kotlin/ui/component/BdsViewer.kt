package ui.component

import PackManager
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import fs.IFile
import fs.IPath

@Composable
fun BdsViewer(modifier: Modifier, manager: PackManager) {
    var items by remember { mutableStateOf<List<IPath<*, *, *>>?>(null) }
    LaunchedEffect(manager) {
        items = manager.root.getItems()
    }

    ElevatedCard(modifier.fillMaxWidth().animateContentSize()) {
        items?.forEach {
            ListItem(
                modifier = Modifier.clickable {},
                headlineContent = { Text(it.name) },
                leadingContent = { Icon(if (it is IFile) Icons.Rounded.Description else Icons.Rounded.Folder, null) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
    }
}
