package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseGradientSlider
import com.aipose.camera.posematch.ui.theme.PoseTextOverlay
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import com.aipose.camera.posematch.util.bidiIsolate
import kotlin.math.roundToInt

private val CardShape = RoundedCornerShape(18.dp)
private const val CARD_GLOSS_LAYER_ALPHA = 0.4f
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
            .cameraGlass(
                shape = CardShape,
                scrimAlpha = CAMERA_CARD_SCRIM_ALPHA,
                borderColor = Color.White.copy(alpha = CAMERA_CARD_BORDER_ALPHA),
                glossLayerAlpha = CARD_GLOSS_LAYER_ALPHA,
                glossInset = 8.dp,
            )
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.camera_overlay_opacity),
                style = poseTextStyle(8.5.sp, FontWeight.Bold, PoseTextOverlay),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.score_percent, (opacity * PERCENT_FACTOR).roundToInt()).bidiIsolate(),
                style = poseTextStyle(11.sp, FontWeight.Bold, Color.White),
                maxLines = 1,
            )
        }
        PoseGradientSlider(
            value = opacity,
            onValueChange = onOpacityChange,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
