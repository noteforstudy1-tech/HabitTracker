package com.habittracker.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Dark Spreadsheet Palette ──────────────────────────────────────────────────
val BackgroundDark      = Color(0xFF0D0D0F)   // Near-black canvas
val SurfaceDark         = Color(0xFF161619)   // Card / panel surface
val SurfaceVariantDark  = Color(0xFF1E1E23)   // Elevated rows
val BorderColor         = Color(0xFF2A2A32)   // Grid cell borders
val BorderAccent        = Color(0xFF3A3A45)   // Highlighted borders

val TextPrimary         = Color(0xFFF0F0F5)   // Crisp white text
val TextSecondary       = Color(0xFF9090A8)   // Muted labels
val TextHint            = Color(0xFF55556A)   // Placeholder / hint

// ── Accent Colours ────────────────────────────────────────────────────────────
val AccentPurple        = Color(0xFF7C3AED)
val AccentPurpleLight   = Color(0xFFA855F7)
val AccentCyan          = Color(0xFF06B6D4)
val AccentGreen         = Color(0xFF10B981)
val AccentRed           = Color(0xFFEF4444)
val AccentAmber         = Color(0xFFF59E0B)

// ── Chart Bar Gradient ────────────────────────────────────────────────────────
val BarColorStart       = Color(0xFF7C3AED)
val BarColorEnd         = Color(0xFF06B6D4)

// ── Mood Palette ──────────────────────────────────────────────────────────────
val MoodColors = listOf(
    Color(0xFFEF4444),   // Awful  – red
    Color(0xFFF97316),   // Bad    – orange
    Color(0xFFFBBF24),   // Okay   – amber
    Color(0xFF4ADE80),   // Good   – green
    Color(0xFF818CF8)    // Great  – indigo
)

val HabitAccentColors = listOf(
    "#E11D48", "#7C3AED", "#0891B2", "#059669",
    "#D97706", "#DB2777", "#2563EB", "#16A34A"
)
