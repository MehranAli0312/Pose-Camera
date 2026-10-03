package com.aipose.camera.posematch.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.models.Icon3DGlyphFit
import com.aipose.camera.posematch.ui.models.Icon3DPalette

private const val REST_TILT = -8f
private const val SWAY_TILT = 4f
private const val SWAY_DURATION_MS = 2400
private const val SPARKLE_ALPHA = 0.85f

@Composable
fun FloatingIcon3D(
    @DrawableRes iconRes: Int,
    palette: Icon3DPalette,
    modifier: Modifier = Modifier,
    iconSize: Dp = 60.dp,
    boxSize: Dp = 84.dp,
    glyphFit: Icon3DGlyphFit = Icon3DGlyphFit.Centered,
    sparkleTint: Color = Color.White,
) {
    val sway = rememberInfiniteTransition(label = "floatingIconSway")
    val tilt by sway.animateFloat(
        initialValue = REST_TILT,
        targetValue = REST_TILT + SWAY_TILT,
        animationSpec = infiniteRepeatable(
            animation = tween(SWAY_DURATION_MS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatingIconTilt",
    )
    Box(
        modifier = modifier.size(boxSize),
        contentAlignment = Alignment.Center,
    ) {
        Icon3D(
            iconRes = iconRes,
            palette = palette,
            size = iconSize,
            glyphFit = glyphFit,
            modifier = Modifier.graphicsLayer { rotationZ = tilt },
        )
        Sparkle(
            tint = sparkleTint,
            size = 14.dp,
            modifier = Modifier.align(Alignment.TopStart),
        )
        Sparkle(
            tint = sparkleTint,
            size = 10.dp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 4.dp),
        )
    }
}

@Composable
private fun Sparkle(tint: Color, size: Dp, modifier: Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_hero_sparkle),
        contentDescription = null,
        tint = tint.copy(alpha = SPARKLE_ALPHA),
        modifier = modifier.size(size),
    )
}
