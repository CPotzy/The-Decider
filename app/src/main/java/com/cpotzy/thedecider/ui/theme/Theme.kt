package com.cpotzy.thedecider.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ColorScheme = lightColorScheme(
    background = SurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
)

@Composable
fun TheDeciderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = AppTypography,
        content = content,
    )
}
