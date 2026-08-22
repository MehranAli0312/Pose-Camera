package com.aipose.camera.posematch.ui.theme

/**
 * App palette as plain ARGB ints (no Compose). The whole UI is XML/Views now, so each screen reads
 * the persisted theme name and pulls its colours from here. Values match the former Compose theme.
 */
data class AppPalette(
    val accent: Int,
    val accentSecondary: Int,
    val bgTop: Int,
    val bgBottom: Int,
    val card: Int,
    val glass: Int
)

/** Dark-based palettes with distinct moods, keyed by the persisted theme name. */
fun paletteFor(themeName: String): AppPalette = when (themeName) {
    "Light" -> AppPalette(
        accent = 0xFF6366F1.toInt(),
        accentSecondary = 0xFF8B5CF6.toInt(),
        bgTop = 0xFF23232B.toInt(),
        bgBottom = 0xFF1B1B22.toInt(),
        card = 0xFF2E2E38.toInt(),
        glass = 0x1FFFFFFF.toInt()
    )
    "Sleek Charcoal" -> AppPalette(
        accent = 0xFFC08457.toInt(),
        accentSecondary = 0xFFE0A45E.toInt(),
        bgTop = 0xFF14120F.toInt(),
        bgBottom = 0xFF0C0B09.toInt(),
        card = 0xFF1E1B17.toInt(),
        glass = 0x12FFFFFF.toInt()
    )
    "Cyberpunk Violet" -> AppPalette(
        accent = 0xFFD946EF.toInt(),
        accentSecondary = 0xFF7C3AED.toInt(),
        bgTop = 0xFF160C1F.toInt(),
        bgBottom = 0xFF0A0410.toInt(),
        card = 0xFF201430.toInt(),
        glass = 0x14FFFFFF.toInt()
    )
    else -> AppPalette( // Dark (default)
        accent = 0xFF6366F1.toInt(),
        accentSecondary = 0xFF8B5CF6.toInt(),
        bgTop = 0xFF0F0F0F.toInt(),
        bgBottom = 0xFF09090A.toInt(),
        card = 0xFF16161A.toInt(),
        glass = 0x0FFFFFFF.toInt()
    )
}
