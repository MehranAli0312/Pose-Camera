package com.aipose.camera.posematch.ui.theme

import android.content.Context
import androidx.core.content.ContextCompat
import com.aipose.camera.posematch.R

/**
 * App palette as plain ARGB ints, resolved from day/night colour resources. The whole UI is
 * XML/Views now; each screen reads the palette from its (night-mode-configured) context, so Light
 * and Dark are selected automatically by the configuration MainActivity applies from the persisted
 * choice. Media surfaces (camera/editor/success) stay dark in both modes by design.
 */
data class AppPalette(
    val accent: Int,
    val accentSecondary: Int,
    val bgTop: Int,
    val bgBottom: Int,
    val card: Int,
    val glass: Int,
    val textPrimary: Int,
    val textSecondary: Int,
    val iconMuted: Int,
    val navBg: Int,
)

/** Reads the palette for the context's current (day/night) configuration. */
fun paletteFor(context: Context): AppPalette {
    fun c(id: Int) = ContextCompat.getColor(context, id)
    return AppPalette(
        accent = c(R.color.accent),
        accentSecondary = c(R.color.accent_secondary),
        bgTop = c(R.color.bg_top),
        bgBottom = c(R.color.bg_bottom),
        card = c(R.color.card),
        glass = c(R.color.glass),
        textPrimary = c(R.color.text_primary),
        textSecondary = c(R.color.text_secondary),
        iconMuted = c(R.color.icon_muted),
        navBg = c(R.color.nav_bg),
    )
}
