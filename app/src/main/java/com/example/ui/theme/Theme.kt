package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MinimalPurplePrimary,
    onPrimary = MinimalPurpleOnPrimary,
    primaryContainer = MinimalContainer,
    onPrimaryContainer = MinimalOnContainer,
    secondary = MinimalPurplePrimary,
    onSecondary = MinimalPurpleOnPrimary,
    secondaryContainer = MinimalContainer,
    onSecondaryContainer = MinimalOnContainer,
    tertiary = MinimalCodeCyan,
    onTertiary = Color(0xFF00363D),
    tertiaryContainer = MinimalContainer,
    onTertiaryContainer = MinimalOnContainer,
    background = MinimalBackground,
    onBackground = MinimalTextPrimary,
    surface = MinimalSurface,
    onSurface = MinimalTextPrimary,
    surfaceVariant = MinimalSurfaceElevated,
    onSurfaceVariant = MinimalTextSecondary,
    outline = MinimalSurfaceBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006874),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF97F0FF),
    onPrimaryContainer = Color(0xFF001F24),
    secondary = Color(0xFF5B3BB4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEADBFF),
    onSecondaryContainer = Color(0xFF1B0047),
    tertiary = Color(0xFF006D35),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF6CFFA0),
    onTertiaryContainer = Color(0xFF00210C),
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightSurfaceBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek cyberpunk dark mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
