package com.habittracker.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary          = AccentPurple,
    onPrimary        = TextPrimary,
    primaryContainer = SurfaceVariantDark,
    secondary        = AccentCyan,
    onSecondary      = TextPrimary,
    background       = BackgroundDark,
    onBackground     = TextPrimary,
    surface          = SurfaceDark,
    onSurface        = TextPrimary,
    surfaceVariant   = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline          = BorderColor,
    error            = AccentRed,
    onError          = TextPrimary
)

@Composable
fun HabitTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = AppTypography,
        content     = content
    )
}
