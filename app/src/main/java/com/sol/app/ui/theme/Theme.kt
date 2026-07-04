package com.sol.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SolViolet,
    onPrimary = SolOnPrimary,
    primaryContainer = SolLavande,
    secondary = SolVioletLight,
    onSecondary = Color.White,
    secondaryContainer = SolLavande,
    background = SolBackground,
    surface = SolSurface,
    onBackground = SolTextPrimary,
    onSurface = SolTextPrimary,
    onSurfaceVariant = SolTextSecondary,
    error = SolError,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = SolVioletNight,
    onPrimary = Color.Black,
    primaryContainer = SolVioletDark,
    secondary = SolVioletLight,
    onSecondary = Color.Black,
    secondaryContainer = SolVioletDark,
    background = SolBackgroundNight,
    surface = SolSurfaceNight,
    onBackground = SolTextPrimaryNight,
    onSurface = SolTextPrimaryNight,
    onSurfaceVariant = SolTextSecondaryNight,
    error = Color(0xFFEF5350),
    onError = Color.Black,
)

@Composable
fun SolTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
