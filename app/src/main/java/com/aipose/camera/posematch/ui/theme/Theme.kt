package com.aipose.camera.posematch.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Live, app-wide palette. Shared color "constants" in the UI layer read from here (via property
 * getters), so flipping [AppThemeState] re-grades the whole app without touching each call site.
 */
object AppThemeState {
    var accent by mutableStateOf(Color(0xFF6366F1))
    var accentSecondary by mutableStateOf(Color(0xFF8B5CF6))
    var bgTop by mutableStateOf(Color(0xFF0F0F0F))
    var bgBottom by mutableStateOf(Color(0xFF09090A))
    var card by mutableStateOf(Color(0xFF16161A))
    var glass by mutableStateOf(Color(0x0FFFFFFF))
}

data class AppPalette(
    val accent: Color,
    val accentSecondary: Color,
    val bgTop: Color,
    val bgBottom: Color,
    val card: Color,
    val glass: Color
)

/** Themes kept on a dark base so the existing light text stays readable, but with distinct moods. */
fun paletteFor(themeName: String): AppPalette = when (themeName) {
    "Light" -> AppPalette(
        accent = Color(0xFF6366F1),
        accentSecondary = Color(0xFF8B5CF6),
        bgTop = Color(0xFF23232B),
        bgBottom = Color(0xFF1B1B22),
        card = Color(0xFF2E2E38),
        glass = Color(0x1FFFFFFF)
    )
    "Sleek Charcoal" -> AppPalette(
        accent = Color(0xFFC08457),
        accentSecondary = Color(0xFFE0A45E),
        bgTop = Color(0xFF14120F),
        bgBottom = Color(0xFF0C0B09),
        card = Color(0xFF1E1B17),
        glass = Color(0x12FFFFFF)
    )
    "Cyberpunk Violet" -> AppPalette(
        accent = Color(0xFFD946EF),
        accentSecondary = Color(0xFF7C3AED),
        bgTop = Color(0xFF160C1F),
        bgBottom = Color(0xFF0A0410),
        card = Color(0xFF201430),
        glass = Color(0x14FFFFFF)
    )
    else -> AppPalette( // Dark (default)
        accent = Color(0xFF6366F1),
        accentSecondary = Color(0xFF8B5CF6),
        bgTop = Color(0xFF0F0F0F),
        bgBottom = Color(0xFF09090A),
        card = Color(0xFF16161A),
        glass = Color(0x0FFFFFFF)
    )
}

@Composable
fun MyApplicationTheme(
    themeName: String = "Dark",
    content: @Composable () -> Unit,
) {
    val palette = remember(themeName) { paletteFor(themeName) }

    SideEffect {
        AppThemeState.accent = palette.accent
        AppThemeState.accentSecondary = palette.accentSecondary
        AppThemeState.bgTop = palette.bgTop
        AppThemeState.bgBottom = palette.bgBottom
        AppThemeState.card = palette.card
        AppThemeState.glass = palette.glass
    }

    val colorScheme = darkColorScheme(
        primary = palette.accent,
        secondary = palette.accentSecondary,
        tertiary = Color(0xFF10B981),
        background = palette.bgBottom,
        surface = palette.card,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color(0xFFF1F5F9),
        onSurface = Color(0xFFF1F5F9)
    )

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
