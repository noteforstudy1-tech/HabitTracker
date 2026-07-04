package com.habittracker.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalDarkTheme = compositionLocalOf { false }

// ── Inline White — defined first so color schemes can reference it ────────────
private val White = Color(0xFFFFFFFF)

// ── Dark Scheme ───────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary               = AccentPurple,
    onPrimary             = White,
    primaryContainer      = GlassBgElevatedDark,
    onPrimaryContainer    = TextPrimaryDark,
    secondary             = AccentCyan,
    onSecondary           = White,
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
    onError               = White
)

// ── Light Scheme ──────────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary               = AccentPurple,
    onPrimary             = White,
    primaryContainer      = GlassBgElevatedLight,
    onPrimaryContainer    = TextPrimaryLight,
    secondary             = AccentCyan,
    onSecondary           = White,
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
    onError               = White
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
