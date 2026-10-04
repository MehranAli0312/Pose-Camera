package com.aipose.camera.posematch.ui.screens.collections.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.common.PoseScorePill
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.screens.collections.models.CaptureUi
import com.aipose.camera.posematch.ui.theme.PosePinkSoft

private val CardShape = RoundedCornerShape(20.dp)
private val HeartBadgeSize = 28.dp
private val HeartGlyphSize = 12.dp
private const val CARD_ASPECT_RATIO = 110f / 150f
private const val SCRIM_MID_STOP = 0.55f
private const val SCRIM_MID_ALPHA = 0.08f
private const val SCRIM_END_ALPHA = 0.78f
private const val HEART_FILL_ALPHA = 0.45f
private const val HEART_BORDER_ALPHA = 0.22f

@Composable
internal fun CaptureShotCard(
    captureUi: CaptureUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(CARD_ASPECT_RATIO)
            .clip(CardShape)
            .bounceClick(onClick = onClick),
    ) {
        PoseImage(
            imagePath = captureUi.capture.imagePath,
            contentDescription = captureUi.titleLabel,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        SCRIM_MID_STOP to Color.Black.copy(alpha = SCRIM_MID_ALPHA),
                        1f to Color.Black.copy(alpha = SCRIM_END_ALPHA),
                    )
                ),
        )
        if (captureUi.capture.isFavorite) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 10.dp)
                    .size(HeartBadgeSize)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = HEART_FILL_ALPHA))
                    .border(1.dp, Color.White.copy(alpha = HEART_BORDER_ALPHA), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_pose_heart),
                    contentDescription = stringResource(R.string.action_favorite),
                    colorFilter = ColorFilter.tint(PosePinkSoft),
                    modifier = Modifier.size(HeartGlyphSize),
                )
            }
        }
        PoseScorePill(
            score = captureUi.capture.matchScore,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, bottom = 10.dp),
        )
    }
}
