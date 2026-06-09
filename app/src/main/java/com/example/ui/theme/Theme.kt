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

@Composable
fun NothingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isMonochrome: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = if (isMonochrome) MonochromeColorScheme else NaturalColorScheme,
        typography = AppTypography,
        content = content
    )
}
