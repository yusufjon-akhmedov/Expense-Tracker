package com.yusufjonaxmedov.pennywise.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    tertiary = WarmAccent,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceTintLight,
    error = ErrorLight,
)

private val DarkColors = darkColorScheme(
    primary = GreenPrimaryDark,
    secondary = GreenSecondaryDark,
    tertiary = WarmAccentDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceTintDark,
    error = ErrorDark,
)

@Composable
fun PennywiseTheme(
    forceDarkTheme: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val darkTheme = forceDarkTheme ?: isSystemInDarkTheme()
    val context = LocalContext.current
    val colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && forceDarkTheme == null) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = PennywiseTypography,
        content = content,
    )
}
