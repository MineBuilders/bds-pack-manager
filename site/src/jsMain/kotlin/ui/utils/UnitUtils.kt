package ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity

private typealias Pixel = Int

@Composable
fun Pixel.toDp() = LocalDensity.current.run { toDp() }
