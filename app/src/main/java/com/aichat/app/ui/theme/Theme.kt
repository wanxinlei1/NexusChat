package com.aichat.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary            = Brand,
    onPrimary          = Color.White,
    primaryContainer   = BrandSurface,
    onPrimaryContainer = BrandDark,
    secondary          = Color(0xFF0EA5E9),
    onSecondary        = Color.White,
    background         = LightBackground,
    onBackground       = LightOnBackground,
    surface            = LightSurface,
    onSurface          = LightOnSurface,
    surfaceVariant     = LightSurfaceVariant,
    onSurfaceVariant   = LightOnSurfaceVar,
    outline            = LightOutline,
    error              = LightError,
    onError            = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary            = BrandLight,
    onPrimary          = Color(0xFF1E1B4B),
    primaryContainer   = BrandDark,
    onPrimaryContainer = BrandSurface,
    secondary          = Color(0xFF38BDF8),
    onSecondary        = Color(0xFF0C4A6E),
    background         = DarkBackground,
    onBackground       = DarkOnBackground,
    surface            = DarkSurface,
    onSurface          = DarkOnSurface,
    surfaceVariant     = DarkSurfaceVariant,
    onSurfaceVariant   = DarkOnSurfaceVar,
    outline            = DarkOutline,
    error              = DarkError,
    onError            = Color(0xFF450A0A),
)

@Composable
fun NexusChatTheme(
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
