package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseGradientSlider
import com.aipose.camera.posematch.ui.theme.PoseTextOverlay
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val CardShape = RoundedCornerShape(14.dp)
private const val SCRIM_ALPHA = 0.5f
private const val BORDER_ALPHA = 0.1f
private const val PERCENT_FACTOR = 100

@Composable
internal fun OverlayOpacitySlider(
    opacity: Float,
    onOpacityChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(Color.Black.copy(alpha = SCRIM_ALPHA))
            .border(1.dp, Color.White.copy(alpha = BORDER_ALPHA), CardShape)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.camera_overlay_opacity),
                style = poseTextStyle(8.5.sp, FontWeight.Bold, PoseTextOverlay),
            )
            Text(
                text = stringResource(
                    R.string.score_percent,
                    (opacity * PERCENT_FACTOR).toInt(),
                ),
                style = poseTextStyle(11.sp, FontWeight.Bold, Color.White),
            )
        }
        PoseGradientSlider(
            value = opacity,
            onValueChange = onOpacityChange,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
