package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ── PRIMARY DESIGN PALETTE (new UI language) ──
val HavenCyan = Color(0xFF49CBEB)          // Primary accent — cyan-blue
val HavenMint = Color(0xFF00EBC7)          // Secondary accent — mint teal
val HavenBgLight = Color(0xFFFFFFFF)       // Light bg
val HavenBgNearWhite = Color(0xFFF4F5F6)   // Light surface
val HavenDarkBg = Color(0xFF1C1C1E)        // Dark background
val HavenDarkSurface = Color(0xFF2C2C2E)   // Dark card surface
val HavenDarkSurface2 = Color(0xFF3A3A3C)  // Dark chip unselected
val HavenTextDark = Color(0xFF000000)      // Primary text on light
val HavenTextLight = Color(0xFFFFFFFF)     // Primary text on dark
val HavenTextSecondary = Color(0xFF8A8A8E) // Secondary text
val HavenBorder = Color(0xFFE5E5EA)        // Divider / border
val HavenGreen = Color(0xFF34C759)         // Success green (iOS-style)
val HavenRed = Color(0xFFFF3B30)           // Error/destructive red (iOS-style)
val HavenOrange = Color(0xFFFF9500)        // Warning orange
val HavenChipSelected = HavenCyan          // Active chip bg
val HavenChipUnselectedDark = Color(0xFF3A3A3C) // Unselected chip on dark
val HavenChipUnselectedLight = Color(0xFFE5E5EA) // Unselected chip on light

// NATURAL PALETTE (Warm Grey / Sand)
val NtBackground = Color(0xFFFDF8F6)
val NtOnBackground = Color(0xFF1F1B1A)
val NtSurface = Color(0xFFE7E1DE)
val NtSurfaceVariant = Color(0xFFF5EBE8)
val NtPrimary = Color(0xFFFFDAD4)
val NtOnPrimary = Color(0xFF3A0905)
val NtSecondary = Color(0xFF855348)
val NtTextSecondary = Color(0xFF524441)
val NtTextTertiary = Color(0xFF735A56)
val NtBorder = Color(0xFFD7C2BE)
val NtGreen = Color(0xFFDDE5D9)
val NtGreenText = Color(0xFF3B4D39)

val MonoBackground = Color(0xFF000000)
val MonoOnBackground = Color(0xFFFFFFFF)
val MonoSurface = Color(0xFF1C1C1C)
val MonoSurfaceVariant = Color(0xFF2C2C2C)
val MonoPrimary = Color(0xFFFFFFFF)
val MonoOnPrimary = Color(0xFF000000)
val MonoSecondary = Color(0xFFAAAAAA)
val MonoTextSecondary = Color(0xFFAAAAAA)
val MonoTextTertiary = Color(0xFF777777)
val MonoBorder = Color(0xFF333333)
val MonoGreen = Color(0xFF2C2C2C)
val MonoGreenText = Color(0xFFFFFFFF)

// Amber Palette (Warm Dark)
val AmberBackground = Color(0xFF12100E)
val AmberOnBackground = Color(0xFFF5EFEB)
val AmberSurface = Color(0xFF1E1A16)
val AmberSurfaceVariant = Color(0xFF2C2722)
val AmberPrimary = Color(0xFFFFB300)
val AmberOnPrimary = Color(0xFF12100E)
val AmberSecondary = Color(0xFFFFA000)
val AmberBorder = Color(0xFF423B35)

// Forest Palette (Green Dark)
val ForestBackground = Color(0xFF0E1210)
val ForestOnBackground = Color(0xFFEBEFEF)
val ForestSurface = Color(0xFF161E1A)
val ForestSurfaceVariant = Color(0xFF222C27)
val ForestPrimary = Color(0xFF81C784)
val ForestOnPrimary = Color(0xFF0E1210)
val ForestSecondary = Color(0xFF4CAF50)
val ForestBorder = Color(0xFF35423B)

// Ocean Palette (Blue Dark)
val OceanBackground = Color(0xFF0E1012)
val OceanOnBackground = Color(0xFFEBEFEF)
val OceanSurface = Color(0xFF161A1E)
val OceanSurfaceVariant = Color(0xFF22272C)
val OceanPrimary = Color(0xFF4FC3F7)
val OceanOnPrimary = Color(0xFF0E1012)
val OceanSecondary = Color(0xFF0288D1)
val OceanBorder = Color(0xFF353C42)

// Neon Palette (Cyber Synthwave Glow)
val NeonBackground = Color(0xFF070B14)
val NeonOnBackground = Color(0xFF39FF14) // Electric Neon Green
val NeonSurface = Color(0xFF0F172A)
val NeonSurfaceVariant = Color(0xFF1E293B)
val NeonPrimary = Color(0xFF39FF14) // Electric Cyan/Green Glow
val NeonOnPrimary = Color(0xFF070B14)
val NeonSecondary = Color(0xFFFF007F) // Hot Neon Pink Accent
val NeonBorder = Color(0xFF1E293B)

// Dummy mappings to existing names so we don't break usages where imported
val PitchBlack = HavenDarkBg
val DarkGray = HavenDarkSurface
val PureWhite = HavenBgLight
val PaleGray = HavenBgNearWhite
val NeutralGray = HavenTextSecondary
val BorderDark = HavenDarkSurface2
val BorderLight = HavenBorder
val NothingRed = HavenCyan
val AccentRed = HavenMint
val SurfaceDark = HavenDarkSurface
val SurfaceLight = HavenBgNearWhite

