package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NavyLight,
    onPrimary = PureWhite,
    primaryContainer = NavySecondary,
    onPrimaryContainer = PureWhite,
    secondary = ElectricCyan,
    onSecondary = PureWhite,
    background = DarkNavyBackground,
    onBackground = DarkTextPrimary,
    surface = DarkNavySurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkNavyCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = SlateSubtext
)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = PureWhite,
    primaryContainer = NavySecondary,
    onPrimaryContainer = PureWhite,
    secondary = NavySecondary,
    onSecondary = PureWhite,
    background = SlateBackground,
    onBackground = CharcoalText,
    surface = PureWhite,
    onSurface = CharcoalText,
    surfaceVariant = SlateBackground,
    onSurfaceVariant = SlateSubtext,
    outline = SlateBorder
)

@Composable
fun NijoPvtTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
