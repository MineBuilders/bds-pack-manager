package ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowLeft
import androidx.compose.material.icons.automirrored.rounded.ArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

class MenuNode(
    val text: String,
    private val leadingIcon: ImageVector? = null,
    private val leadingText: String? = null,
    private val trailingIcon: ImageVector? = null,
    private val trailingText: String? = null,
    val trailingFunctional: Boolean = false,
    private val onClick: () -> Unit = {},
    val builder: @Composable MenuNode.() -> Unit = {}
) : MenuTree() {
    @Composable
    fun build() {
        children.clear()
        builder()
    }

    @Composable
    fun show(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) = DropdownMenuItem(
        text = { Text(text) },
        onClick = { this@MenuNode.onClick(); onClick?.invoke(); },
        modifier = modifier,
        leadingIcon = leadingIcon?.let { { Icon(leadingIcon, leadingText) } }
            ?: leadingText?.let { { Text(leadingText, textAlign = TextAlign.Center) } },
        trailingIcon = trailingIcon?.let {
            {
                Icon(
                    trailingIcon,
                    trailingText,
                    tint = if (trailingFunctional) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
        }
            ?: trailingText?.let { { Text(trailingText, textAlign = TextAlign.Center) } }
            ?: (@Composable { Icon(Icons.AutoMirrored.Rounded.ArrowRight, null) }).takeIf { children.isNotEmpty() },
    )
}

open class MenuTree {
    var parent: MenuTree? = null
    val children = mutableListOf<MenuNode?>()

    fun divider() = children.add(null)

    @Composable
    operator fun MenuNode.unaryPlus() =
        this@MenuTree.children.add(this.also { it.build(); it.parent = this@MenuTree })
}

@Composable
fun NestedDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset.Zero,
    builder: @Composable MenuTree.() -> Unit,
) {
    val root = MenuTree().apply { builder() }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.animateContentSize(),
        offset = offset,
    ) {
        var current by remember { mutableStateOf(root) }
        if (current.parent != null) DropdownMenuItem(
            text = { Text((current as MenuNode).text, Modifier.offset(x = (-16).dp)) },
            onClick = { current = current.parent!! },
            modifier = Modifier.alpha(.8f).heightIn(max = 32.dp),
            leadingIcon = { Icon(Icons.AutoMirrored.Rounded.ArrowLeft, null, Modifier.offset(x = (-8).dp)) },
        )
        current.children.forEach { child ->
            if (child == null) return@forEach HorizontalDivider()
            child.build()
            child.show {
                if (child.children.isNotEmpty()) current = child
                else /*if (!child.trailingFunctional)*/ {
                    onDismissRequest()
                    current = root
                }
            }
        }
    }
}
