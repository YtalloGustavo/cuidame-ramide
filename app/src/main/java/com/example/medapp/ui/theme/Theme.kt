package com.example.medapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = White,
    primaryContainer = GreenLight,
    onPrimaryContainer = GreenPrimary,
    secondary = GreenMid,
    background = Background,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = Background,
    onSurfaceVariant = Ink2,
    outline = Border,
    error = Red,
    onError = White,
    errorContainer = RedLight,
    onErrorContainer = Red
)

@Composable
fun MedAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Background.toArgb()
            window.navigationBarColor = Background.toArgb()
            
            // Set light status bar so icons are dark and readable
            val decorView = window.decorView
            val wic = androidx.core.view.WindowCompat.getInsetsController(window, decorView)
            wic.isAppearanceLightStatusBars = true
            wic.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
