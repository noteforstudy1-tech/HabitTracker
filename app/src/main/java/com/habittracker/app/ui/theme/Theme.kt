package com.habittracker.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

// Composition local so any composable can read the current dark mode state
val LocalDarkTheme = compositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary               = AccentPurple,
    onPrimary             = TextPrimaryDark,
    primaryContainer      = GlassBgElevatedDark,
    onPrimaryContainer    = TextPrimaryDark,
    secondary             = AccentCyan,
    onSecondary           = TextPrimaryDark,
    secondaryContainer    = GlassBgDark,
    onSecondaryContainer  = TextSecondaryDark,
    background            = BgGradientStartDark,
    onBackground          = TextPrimaryDark,
    surface               = GlassBgDark,
    onSurface             = TextPrimaryDark,
    surfaceVariant        = GlassBgElevatedDark,
    onSurfaceVariant      = TextSecondaryDark,
    outline               = GlassBorderDark,
    error                 = AccentRed,
    onError               = TextPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary               = AccentPurple,
    onPrimary             = TextPrimaryLight,
    primaryContainer      = GlassBgElevatedLight,
    onPrimaryContainer    = TextPrimaryLight,
    secondary             = AccentCyan,
    onSecondary           = TextPrimaryLight,
    secondaryContainer    = GlassBgLight,
    onSecondaryContainer  = TextSecondaryLight,
    background            = BgGradientStartLight,
    onBackground          = TextPrimaryLight,
    surface               = GlassBgLight,
    onSurface             = TextPrimaryLight,
    surfaceVariant        = GlassBgElevatedLight,
    onSurfaceVariant      = TextSecondaryLight,
    outline               = GlassBorderLight,
    error                 = AccentRed,
    onError               = TextPrimaryLight
)

@Composable
fun HabitTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colors,
            typography  = AppTypography,
            content     = content
        )
    }
}
