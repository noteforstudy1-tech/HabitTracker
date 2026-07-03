package com.habittracker.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Dark Mode Glassmorphism Palette ───────────────────────────────────────────
val BgGradientStartDark  = Color(0xFF0B061A) // Very deep purple-black
val BgGradientMidDark    = Color(0xFF140728) // Deep amethyst-black
val BgGradientEndDark    = Color(0xFF040209) // Ultra dark slate

val GlassBgDark          = Color(0x1CFFFFFF) // Frosted translucent white
val GlassBgElevatedDark  = Color(0x28FFFFFF) // Slightly less transparent for highlights
val GlassBorderDark      = Color(0x26FFFFFF) // Subtle thin light border

val TextPrimaryDark      = Color(0xFFF5F2F9) // Crisp light purple-white
val TextSecondaryDark    = Color(0xFFB5AEC4) // Muted lavender grey
val TextHintDark         = Color(0xFF756E84) // Placeholder / disabled

// ── Light Mode Glassmorphism Palette ──────────────────────────────────────────
val BgGradientStartLight = Color(0xFFE8DDFC) // Light pastel lavender
val BgGradientMidLight   = Color(0xFFF4EFFF) // Pale lilac
val BgGradientEndLight   = Color(0xFFDCC8FB) // Richer pastel purple

val GlassBgLight         = Color(0x73FFFFFF) // Semi-transparent white
val GlassBgElevatedLight = Color(0x99FFFFFF) // Brighter white glass
val GlassBorderLight     = Color(0x407C3AED) // Muted purple-tinted border

val TextPrimaryLight     = Color(0xFF1D0E30) // Dark eggplant text
val TextSecondaryLight   = Color(0xFF5A4D73) // Muted slate purple
val TextHintLight        = Color(0xFF9F95B5) // Muted placeholder

// ── Core App Accents ──────────────────────────────────────────────────────────
val AccentPurple         = Color(0xFF8B5CF6) // Vibrant violet
val AccentPurpleLight    = Color(0xFFC084FC) // Light violet glow
val AccentCyan           = Color(0xFF06B6D4) // Neon teal
val AccentGreen          = Color(0xFF10B981) // Emerald green
val AccentRed            = Color(0xFFEF4444) // Bright coral red
val AccentAmber          = Color(0xFFF59E0B) // Amber yellow

// ── Mood Colors (Glassmorphic Tint) ───────────────────────────────────────────
val MoodColors = listOf(
    Color(0xFFF43F5E), // Awful - Rose
    Color(0xFFFB923C), // Bad - Orange
    Color(0xFFFBBF24), // Okay - Amber
    Color(0xFF34D399), // Good - Emerald
    Color(0xFF60A5FA)  // Great - Blue
)

val HabitAccentColors = listOf(
    "#EC4899", "#8B5CF6", "#06B6D4", "#10B981",
    "#F59E0B", "#EF4444", "#3B82F6", "#EC4899"
)
