package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SleekBlue500,
    onPrimary = Color.White,
    secondary = SleekBlue100,
    onSecondary = Color.Black,
    tertiary = SafeAmber,
    onTertiary = Color.Black,
    background = ObsidianDark,
    onBackground = TextPrimary,
    surface = ObsidianCard,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianBorder,
    onSurfaceVariant = TextSecondary,
    error = SoftRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SleekBlue600,
    onPrimary = Color.White,
    secondary = SleekBlue500,
    onSecondary = Color.White,
    tertiary = SafeAmber,
    onTertiary = Color.Black,
    background = LightBackground,
    onBackground = SleekTextSlate900,
    surface = LightCard,
    onSurface = SleekTextSlate900,
    surfaceVariant = LightBorderSoft,
    onSurfaceVariant = SleekTextSlate500,
    error = SoftRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
