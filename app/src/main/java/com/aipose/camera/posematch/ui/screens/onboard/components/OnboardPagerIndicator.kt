package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.theme.AppMainColor
import com.aipose.camera.posematch.ui.theme.brandGradientBackground

private val DotSize = 8.dp
private val ActiveDotWidth = 36.dp

@Composable
internal fun OnboardPagerIndicator(
    totalPages: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalPages) { index ->
            val selected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (selected) ActiveDotWidth else DotSize,
                label = "onboardIndicatorWidth",
            )
            val dotModifier = Modifier
                .height(DotSize)
                .width(width)
                .clip(CircleShape)
            Box(
                modifier = if (selected) {
                    dotModifier.brandGradientBackground(CircleShape)
                } else {
                    dotModifier.background(AppMainColor.copy(alpha = 0.2f))
                },
            )
        }
    }
}
