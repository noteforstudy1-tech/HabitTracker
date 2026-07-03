package com.habittracker.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary          = AccentPurple,
    onPrimary        = TextPrimaryDark,
    primaryContainer = GlassBgElevatedDark,
    secondary        = AccentCyan,
    onSecondary      = TextPrimaryDark,
    background       = BgGradientStartDark,
    onBackground     = TextPrimaryDark,
    surface          = GlassBgDark,
    onSurface        = TextPrimaryDark,
    surfaceVariant   = GlassBgElevatedDark,
    onSurfaceVariant = TextSecondaryDark,
    outline          = GlassBorderDark,
    error            = AccentRed,
    onError          = TextPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary          = AccentPurple,
    onPrimary        = TextPrimaryLight,
    primaryContainer = GlassBgElevatedLight,
    secondary        = AccentCyan,
    onSecondary      = TextPrimaryLight,
    background       = BgGradientStartLight,
    onBackground     = TextPrimaryLight,
    surface          = GlassBgLight,
    onSurface        = TextPrimaryLight,
    surfaceVariant   = GlassBgElevatedLight,
    onSurfaceVariant = TextSecondaryLight,
    outline          = GlassBorderLight,
    error            = AccentRed,
    onError          = TextPrimaryLight
)

@Composable
fun HabitTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography  = AppTypography,
        content     = content
    )
}
