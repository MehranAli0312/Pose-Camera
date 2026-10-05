package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.PoseTextMuted
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import androidx.compose.ui.unit.dp

private val BarHeight = 36.dp
private val SkipSize = 13.5.sp

@Composable
internal fun OnboardTopBar(
    showSkip: Boolean,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BarHeight),
        contentAlignment = Alignment.CenterEnd,
    ) {
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
