package com.example.solify.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Brown300,
    onPrimary = LightYellow200,
    primaryContainer = Ocher200,
    onPrimaryContainer = Brown300,
    secondary = Scarlet200,
    onSecondary = LightYellow200,
    secondaryContainer = White200,
    onSecondaryContainer = White100,
    tertiary = Scarlet100,
    onTertiary = Ocher100,
    tertiaryContainer = Brown200,
    onTertiaryContainer = Brown100
)

private val LightColorScheme = lightColorScheme(
    primary = White300,
    onPrimary = Brown300,
    primaryContainer = Yellow200,
    onPrimaryContainer = Yellow200,
    secondary = Burgundy200,
    onSecondary = Burgundy200,
    secondaryContainer = Burgundy100,
    onSecondaryContainer = Black100,
    tertiary = Burgundy100,
    onTertiary = Yellow100,
    tertiaryContainer = Yellow100,
    onTertiaryContainer = LightYellow100
)

@Composable
fun SolifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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