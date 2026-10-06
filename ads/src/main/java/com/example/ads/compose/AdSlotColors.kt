package com.example.ads.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Host-app colors for everything the ads module draws itself: the banner slot behind the
 * AdMob creative, the loading placeholder, and native ad templates. The banner creative
 * itself is rendered by the SDK and cannot be themed.
 */
@Immutable
data class AdSlotColors(
    val container: Color,
    val headline: Color,
    val body: Color,
    val placeholderBone: Color = body.copy(alpha = PLACEHOLDER_BONE_ALPHA),
)

/** Provide from the app theme; when absent, colors are derived from [MaterialTheme]. */
val LocalAdSlotColors = staticCompositionLocalOf<AdSlotColors?> { null }

object AdSlotDefaults {
    val topSpacing: Dp = 8.dp

    val colors: AdSlotColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAdSlotColors.current ?: AdSlotColors(
            container = MaterialTheme.colorScheme.surface,
            headline = MaterialTheme.colorScheme.onSurface,
            body = MaterialTheme.colorScheme.onSurfaceVariant,
        )
}

private const val PLACEHOLDER_BONE_ALPHA = 0.25f
