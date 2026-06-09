package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NaturalColorScheme = lightColorScheme(
    primary = NtPrimary,
    onPrimary = NtOnPrimary,
    primaryContainer = NtPrimary,
    onPrimaryContainer = NtOnPrimary,
    secondary = NtSecondary,
    onSecondary = NtBackground,
    surface = NtSurface,
    onSurface = NtOnBackground,
    background = NtBackground,
    onBackground = NtOnBackground,
    error = NtSecondary,
    outline = NtBorder,
    surfaceVariant = NtSurfaceVariant,
    onSurfaceVariant = NtTextSecondary
)

private val NaturalDarkColorScheme = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF000000),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF000000),
    secondary = NtSecondary,
    onSecondary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    surface = androidx.compose.ui.graphics.Color(0xFF161616),
    onSurface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    background = androidx.compose.ui.graphics.Color(0xFF0C0C0C),
    onBackground = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    error = NtSecondary,
    outline = androidx.compose.ui.graphics.Color(0xFF2C2C2C),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF1C1C1C),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFAAAAAA)
)

private val MonochromeColorScheme = lightColorScheme(
    primary = MonoPrimary,
    onPrimary = MonoOnPrimary,
    primaryContainer = MonoPrimary,
    onPrimaryContainer = MonoOnPrimary,
    secondary = MonoSecondary,
    onSecondary = MonoBackground,
    surface = MonoSurface,
    onSurface = MonoOnBackground,
    background = MonoBackground,
    onBackground = MonoOnBackground,
    error = MonoSecondary,
    outline = MonoBorder,
    surfaceVariant = MonoSurfaceVariant,
    onSurfaceVariant = MonoTextSecondary
)

private val AmberColorScheme = lightColorScheme(
    primary = AmberPrimary,
    onPrimary = AmberOnPrimary,
    primaryContainer = AmberPrimary,
    onPrimaryContainer = AmberOnPrimary,
    secondary = AmberSecondary,
    onSecondary = AmberBackground,
    surface = AmberSurface,
    onSurface = AmberOnBackground,
    background = AmberBackground,
    onBackground = AmberOnBackground,
    error = AmberSecondary,
    outline = AmberBorder,
    surfaceVariant = AmberSurfaceVariant,
    onSurfaceVariant = AmberOnBackground
)

private val ForestColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestOnPrimary,
    primaryContainer = ForestPrimary,
    onPrimaryContainer = ForestOnPrimary,
    secondary = ForestSecondary,
    onSecondary = ForestBackground,
    surface = ForestSurface,
    onSurface = ForestOnBackground,
    background = ForestBackground,
    onBackground = ForestOnBackground,
    error = ForestSecondary,
    outline = ForestBorder,
    surfaceVariant = ForestSurfaceVariant,
    onSurfaceVariant = ForestOnBackground
)

private val OceanColorScheme = lightColorScheme(
    primary = OceanPrimary,
    onPrimary = OceanOnPrimary,
    primaryContainer = OceanPrimary,
    onPrimaryContainer = OceanOnPrimary,
    secondary = OceanSecondary,
    onSecondary = OceanBackground,
    surface = OceanSurface,
    onSurface = OceanOnBackground,
    background = OceanBackground,
    onBackground = OceanOnBackground,
    error = OceanSecondary,
    outline = OceanBorder,
    surfaceVariant = OceanSurfaceVariant,
    onSurfaceVariant = OceanOnBackground
)

private val NeonColorScheme = lightColorScheme(
    primary = NeonPrimary,
    onPrimary = NeonOnPrimary,
    primaryContainer = NeonPrimary,
    onPrimaryContainer = NeonOnPrimary,
    secondary = NeonSecondary,
    onSecondary = NeonBackground,
    surface = NeonSurface,
    onSurface = NeonOnBackground,
    background = NeonBackground,
    onBackground = NeonOnBackground,
    error = NeonSecondary,
    outline = NeonBorder,
    surfaceVariant = NeonSurfaceVariant,
    onSurfaceVariant = NeonOnBackground
)

@Composable
fun NothingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: String = "NATURAL",
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    val selectedScheme = when (palette) {
        "MONOCHROME" -> {
            if (darkTheme) MonochromeColorScheme else lightColorScheme(
                primary = androidx.compose.ui.graphics.Color.Black,
                onPrimary = androidx.compose.ui.graphics.Color.White,
                primaryContainer = androidx.compose.ui.graphics.Color.Black,
                onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
                secondary = androidx.compose.ui.graphics.Color.DarkGray,
                onSecondary = androidx.compose.ui.graphics.Color.White,
                surface = androidx.compose.ui.graphics.Color(0xFFEBEBEB),
                onSurface = androidx.compose.ui.graphics.Color.Black,
                background = androidx.compose.ui.graphics.Color(0xFFF9F9F9),
                onBackground = androidx.compose.ui.graphics.Color.Black,
                error = NtSecondary,
                outline = androidx.compose.ui.graphics.Color(0xFFCCCCCC),
                surfaceVariant = androidx.compose.ui.graphics.Color(0xFFF0F0F0),
                onSurfaceVariant = androidx.compose.ui.graphics.Color.Gray
            )
        }
        "AMBER" -> AmberColorScheme
        "FOREST" -> ForestColorScheme
        "OCEAN" -> OceanColorScheme
        "NEON" -> NeonColorScheme
        else -> {
            if (darkTheme) NaturalDarkColorScheme else NaturalColorScheme
        }
    }

    MaterialTheme(
        colorScheme = selectedScheme,
        typography = AppTypography,
        content = content
    )
}
