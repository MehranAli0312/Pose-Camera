package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.common.PoseCtaHeight
import com.aipose.camera.posematch.ui.common.PoseScreenTopSpacing

internal object OnboardChromeMetrics {
    val TopBarTop: Dp = PoseScreenTopSpacing
    val TopBarHeight: Dp = 36.dp
    val IndicatorTop: Dp = 44.dp
    val IndicatorHeight: Dp = 8.dp
    val IndicatorToCta: Dp = 30.dp
    val CtaBottom: Dp = 36.dp

    val TopHeight: Dp = TopBarTop + TopBarHeight
    val BottomHeight: Dp = IndicatorTop + IndicatorHeight + IndicatorToCta + PoseCtaHeight + CtaBottom
}
