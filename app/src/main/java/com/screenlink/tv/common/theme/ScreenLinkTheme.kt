package com.screenlink.tv.common.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

private val ScreenLinkColors = darkColorScheme(
    primary = Color(0xFF39D98A),
    background = Color.Black,
    surface = Color(0xFF101414),
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun ScreenLinkTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ScreenLinkColors, content = content)
}
