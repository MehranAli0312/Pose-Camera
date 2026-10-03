package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.defaultShimmerTheme
import com.valentinilk.shimmer.rememberShimmer

val shimmerBlockColor: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

@Composable
fun rememberAppShimmer(bounds: ShimmerBounds = ShimmerBounds.View): Shimmer {
    val isDark = isSystemInDarkTheme()
    val theme = remember(isDark) {
        val highlight =
            if (isDark) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.75f)
        defaultShimmerTheme.copy(
            shaderColors = listOf(
                highlight.copy(alpha = 0f),
                highlight,
                highlight.copy(alpha = 0f),
            ),
            shaderColorStops = null,
        )
    }
    return rememberShimmer(shimmerBounds = bounds, theme = theme)
}

@Composable
fun ShimmerBlock(
    modifier: Modifier = Modifier,
    corner: Dp = 6.dp,
    alpha: Float = 0.3f,
    shape: Shape = RoundedCornerShape(corner),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)),
    )
}
