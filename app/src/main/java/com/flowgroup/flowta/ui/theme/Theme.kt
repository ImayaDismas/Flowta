package com.flowgroup.flowta.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = BrandBlueContainer,
    onPrimaryContainer = OnBrandBlueContainer,
    secondary = InkMuted,
    onSecondary = SurfaceWhite,
    background = SurfaceWhite,
    onBackground = Ink,
    surface = SurfaceWhite,
    onSurface = Ink,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = InkMuted,
    outline = OutlineLight,
    error = MoneyOut,
    onError = SurfaceWhite,
)

private val DarkColors = darkColorScheme(
    primary = BrandBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = BrandBlueDark,
    onPrimaryContainer = BrandBlueContainer,
    secondary = InkMutedDark,
    onSecondary = Ink,
    background = SurfaceDark,
    onBackground = InkDark,
    surface = SurfaceDark,
    onSurface = InkDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = InkMutedDark,
    outline = OutlineDark,
    error = MoneyOut,
    onError = SurfaceWhite,
)

@Composable
fun FlowtaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = FlowtaShapes,
        content = content,
    )
}
