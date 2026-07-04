package com.habittracker.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

val LocalDarkTheme = compositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary               = AccentPurple,
    onPrimary             = Color_White,
    primaryContainer      = GlassBgElevatedDark,
    onPrimaryContainer    = TextPrimaryDark,
    secondary             = AccentCyan,
    onSecondary           = Color_White,
    secondaryContainer    = GlassBgDark,
    onSecondaryContainer  = TextSecondaryDark,
    background            = BgGradientStartDark,
    onBackground          = TextPrimaryDark,
    surface               = GlassBgDark,           // Opaque dark — text will be readable
    onSurface             = TextPrimaryDark,        // Always bright text
    surfaceVariant        = GlassBgElevatedDark,
    onSurfaceVariant      = TextSecondaryDark,      // Legible secondary text in dark mode
    outline               = GlassBorderDark,
    error                 = AccentRed,
    onError               = Color_White
)

private val LightColorScheme = lightColorScheme(
    primary               = AccentPurple,
    onPrimary             = Color_White,
    primaryContainer      = GlassBgElevatedLight,
    onPrimaryContainer    = TextPrimaryLight,
    secondary             = AccentCyan,
    onSecondary           = Color_White,
    secondaryContainer    = GlassBgLight,
    onSecondaryContainer  = TextSecondaryLight,
    background            = BgGradientStartLight,
    onBackground          = TextPrimaryLight,
    surface               = GlassBgLight,          // Pure white — fully readable
    onSurface             = TextPrimaryLight,       // Dark eggplant — max contrast on white
    surfaceVariant        = GlassBgElevatedLight,
    onSurfaceVariant      = TextSecondaryLight,     // Darker secondary text
    outline               = GlassBorderLight,
    error                 = AccentRed,
    onError               = Color_White
)

// Convenience constant so it doesn't conflict with androidx Color class
private val Color_White = androidx.compose.ui.graphics.Color.White

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
