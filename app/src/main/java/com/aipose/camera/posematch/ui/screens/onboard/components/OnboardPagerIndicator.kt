package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.theme.Violet

private val DotSize = OnboardChromeMetrics.IndicatorHeight
private val ActiveDotWidth = 22.dp
private val DotSpacing = 7.dp
private const val INACTIVE_ALPHA = 0.22f
private const val INDICATOR_LABEL = "onboardIndicator"

@Composable
internal fun OnboardPagerIndicator(
    totalPages: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DotSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalPages) { index ->
            val selected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (selected) ActiveDotWidth else DotSize,
                label = INDICATOR_LABEL,
            )
            val color by animateColorAsState(
                targetValue = if (selected) Violet else Color.White.copy(alpha = INACTIVE_ALPHA),
                label = INDICATOR_LABEL,
            )
            Box(
                modifier = Modifier
                    .height(DotSize)
                    .width(width)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
