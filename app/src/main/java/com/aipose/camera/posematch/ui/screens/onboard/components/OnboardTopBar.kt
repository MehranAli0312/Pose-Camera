package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.PoseTextBright
import com.aipose.camera.posematch.ui.theme.PoseTextMuted
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val BarHeight = 36.dp
private val BackSize = 36.dp
private val ChevronWidth = 10.dp
private val ChevronHeight = 14.dp
private val SkipSize = 13.5.sp
private const val BACK_FILL_ALPHA = 0.06f
private const val BACK_BORDER_ALPHA = 0.10f

@Composable
internal fun OnboardTopBar(
    showBack: Boolean,
    showSkip: Boolean,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(BarHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(visible = showBack, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .size(BackSize)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = BACK_FILL_ALPHA))
                    .border(1.dp, Color.White.copy(alpha = BACK_BORDER_ALPHA), CircleShape)
                    .bounceClick(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_pose_chevron_start),
                    contentDescription = stringResource(R.string.onboard_back),
                    tint = PoseTextBright,
                    modifier = Modifier.size(width = ChevronWidth, height = ChevronHeight),
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        AnimatedVisibility(visible = showSkip, enter = fadeIn(), exit = fadeOut()) {
            Text(
                text = stringResource(R.string.skip),
                style = poseTextStyle(SkipSize, FontWeight.Bold, PoseTextMuted),
                modifier = Modifier
                    .bounceClick(onClick = onSkip)
                    .padding(horizontal = 4.dp, vertical = 8.dp),
            )
        }
    }
}
