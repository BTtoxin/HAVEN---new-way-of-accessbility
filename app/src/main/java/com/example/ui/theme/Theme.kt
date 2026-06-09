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
        "MONOCHROME" -> MonochromeColorScheme
        "AMBER" -> AmberColorScheme
        "FOREST" -> ForestColorScheme
        "OCEAN" -> OceanColorScheme
        else -> NaturalColorScheme
    }

    MaterialTheme(
        colorScheme = selectedScheme,
        typography = AppTypography,
        content = content
    )
}
