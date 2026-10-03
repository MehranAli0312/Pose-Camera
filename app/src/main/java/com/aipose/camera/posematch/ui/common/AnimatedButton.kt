package com.aipose.camera.posematch.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.theme.brandGradientBackground
import com.valentinilk.shimmer.LocalShimmerTheme
import com.valentinilk.shimmer.defaultShimmerTheme
import com.valentinilk.shimmer.shimmer

@Composable
fun AnimatedButton(
    cornerRadius: Dp = 40.dp,
    enabled: Boolean = true,
    horizontal: Dp = 20.dp,
    vertical: Dp = 10.dp,
    onClick: () -> Unit = {},
    contentBlock: (@Composable () -> Unit)
) {
    val creditCardTheme = defaultShimmerTheme.copy(
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                delayMillis = 1_000,
                easing = LinearEasing,
            ),
        ),
        blendMode = BlendMode.Hardlight,
        rotation = 25f,
        shaderColors = listOf(
            Color.White.copy(alpha = 0.0f),
            Color.White.copy(alpha = 0.6f),
            Color.White.copy(alpha = 0.0f),
        ),
        shaderColorStops = null,
        shimmerWidth = 150.dp,
    )

    val shape = RoundedCornerShape(cornerRadius)
    val throttledOnClick = rememberThrottledClick(onClick)

    CompositionLocalProvider(
        LocalShimmerTheme provides creditCardTheme
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = horizontal, vertical = vertical)
                .shadow(
                    elevation = 8.dp,
                    shape = shape,
                    clip = false,
                    spotColor = MaterialTheme.colorScheme.primary,
                )
                .alpha(if (enabled) 1f else 0.5f)
                .then(
                    if (enabled) {
                        Modifier.bounceClick(onClick = throttledOnClick)
                    } else {
                        Modifier
                    },
                ),
            elevation = CardDefaults.cardElevation(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = shape,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .brandGradientBackground(shape),
            ) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .shimmer(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    contentBlock()
                }
            }
        }
    }
}
