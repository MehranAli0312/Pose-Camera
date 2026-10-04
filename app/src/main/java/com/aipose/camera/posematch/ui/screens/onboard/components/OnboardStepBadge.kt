package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.PoseVioletPale
import com.aipose.camera.posematch.ui.theme.Violet
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val BadgeHeight = 24.dp
private val BadgeShape = RoundedCornerShape(12.dp)
private val BadgePadding = 14.dp
private val BadgeTextSize = 9.sp
private const val BADGE_FILL_ALPHA = 0.16f
private const val BADGE_BORDER_ALPHA = 0.32f

@Composable
internal fun OnboardStepBadge(
    stepNumber: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(BadgeHeight)
            .clip(BadgeShape)
            .background(Violet.copy(alpha = BADGE_FILL_ALPHA))
            .border(1.dp, Violet.copy(alpha = BADGE_BORDER_ALPHA), BadgeShape)
            .padding(horizontal = BadgePadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.onboard_step_label, stepNumber, totalSteps),
            style = poseTextStyle(BadgeTextSize, FontWeight.Bold, PoseVioletPale),
        )
    }
}
