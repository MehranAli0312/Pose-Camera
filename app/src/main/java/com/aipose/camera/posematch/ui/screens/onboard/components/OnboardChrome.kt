package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseCtaButton

@Composable
internal fun OnboardChrome(
    totalSteps: Int,
    currentStep: Int,
    ctaText: String,
    onCta: () -> Unit,
    modifier: Modifier = Modifier,
    bottomReserved: Dp = 0.dp,
) {
    OnboardFrame(modifier = modifier, bottomReserved = bottomReserved) {
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(OnboardChromeMetrics.IndicatorTop))
        OnboardPagerIndicator(
            totalPages = totalSteps,
            currentPage = currentStep,
        )
        Spacer(modifier = Modifier.height(OnboardChromeMetrics.IndicatorToCta))
        PoseCtaButton(
            text = ctaText,
            onClick = onCta,
            trailingIconRes = R.drawable.ic_pose_chevron_cta,
        )
        Spacer(modifier = Modifier.height(OnboardChromeMetrics.CtaBottom))
    }
}
