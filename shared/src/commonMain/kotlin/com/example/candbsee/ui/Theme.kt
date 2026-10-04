package com.example.candbsee.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

object CanDbTheme {
    val BgDark = Color(0xFF0B1120)       // Deep tailwind slate-950
    val BgSidebar = Color(0xFF0F172A)    // Slate 900
    val CardBg = Color(0xFF1E293B)       // Slate 800
    val CardBgElevated = Color(0xFF273549)
    val CardBgHover = Color(0xFF334155)  // Slate 700
    val BorderColor = Color(0xFF334155)
    val BorderLight = Color(0xFF475569)
    
    val TextPrimary = Color(0xFFF8FAFC)  // Slate 50
    val TextSecondary = Color(0xFF94A3B8)// Slate 400
    val TextMuted = Color(0xFF64748B)    // Slate 500
    
    val AccentTeal = Color(0xFF14B8A6)   // Teal 500
    val AccentEmerald = Color(0xFF10B981)// Emerald 500
    val AccentCyan = Color(0xFF06B6D4)   // Cyan 500
    val AccentAmber = Color(0xFFF59E0B)  // Amber 500
    val AccentRose = Color(0xFFF43F5E)   // Rose 500
    val AccentIndigo = Color(0xFF6366F1) // Indigo 500
    val AccentViolet = Color(0xFF8B5CF6) // Violet 500

    val colorScheme = darkColorScheme(
        primary = AccentTeal,
        secondary = AccentCyan,
        tertiary = AccentAmber,
        background = BgDark,
        surface = CardBg,
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = TextPrimary,
        onSurface = TextPrimary,
        outline = BorderColor
    )
}
