package com.aipose.camera.posematch.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private const val FIXED_FONT_SCALE = 1f

private val DarkColorPalette = darkColorScheme(
    primary = AppMainColor,
    onPrimary = White,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = StrokeColorDark,
    onSecondary = TextColorDark,
    secondaryContainer = CardBackgroundDark,
    onSecondaryContainer = TextColorDark,
    tertiary = AppSecondaryColor,
    onTertiary = White,
    tertiaryContainer = SoftAccentSecondaryDark,
    onTertiaryContainer = OnSoftAccentSecondaryDark,
    background = BackgroundDark,
    onBackground = TextColorDark,
    surface = SurfaceBarDark,
    onSurface = TextColorDark,
    surfaceVariant = MessageBackgroundDark,
    onSurfaceVariant = InactiveInputDark,
    outline = StrokeColorDark,
    error = Red,
    onError = White,
)

@Composable
fun MyAppTheme(
    content: @Composable () -> Unit,
) {
    val palette = StudioDarkPalette
    val colorScheme = remember(palette) {
        DarkColorPalette.copy(
            primary = palette.accent,
            secondary = palette.accentSecondary,
            tertiary = Emerald,
            background = palette.backgroundBottom,
            surface = palette.card,
            secondaryContainer = palette.card,
            onBackground = StudioOnBackground,
            onSurface = StudioOnBackground,
        )
    }
    val typography = TypographyDark
    val systemDensity = LocalDensity.current
    val fixedFontScaleDensity = remember(systemDensity.density) {
        Density(density = systemDensity.density, fontScale = FIXED_FONT_SCALE)
    }

    CompositionLocalProvider(
        LocalDensity provides fixedFontScaleDensity,
        LocalAppPalette provides palette,
        LocalExtendedColors provides DarkExtendedColors,
        LocalContentColor provides colorScheme.onBackground,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content,
        )
    }
}

object AppTheme {
    val extendedColors: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current

    val colorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme
}
