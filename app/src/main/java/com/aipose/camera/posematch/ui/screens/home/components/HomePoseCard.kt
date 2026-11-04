package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.common.PoseProBadge
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.click
import com.aipose.camera.posematch.ui.theme.PosePinkSoft

private val CardShape = RoundedCornerShape(18.dp)
private val CardWidth = 150.dp
private const val CARD_ASPECT_RATIO = 150f / 210f
private val HeartSize = 28.dp
private val HeartGlyphSize = 13.dp
private const val SCRIM_MID_STOP = 0.45f
private const val SCRIM_MID_ALPHA = 0.1f
private const val SCRIM_END_ALPHA = 0.85f
private const val HEART_FILL_ALPHA = 0.45f
private const val HEART_BORDER_ALPHA = 0.25f
private const val HEART_INACTIVE_ALPHA = 0.9f

private val CardScrim = Brush.verticalGradient(
    0f to Color.Transparent,
    SCRIM_MID_STOP to Color.Black.copy(alpha = SCRIM_MID_ALPHA),
    1f to Color.Black.copy(alpha = SCRIM_END_ALPHA),
)

@Composable
internal fun HomePoseCard(
    pose: Pose,
    isSaved: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(CardWidth)
            .aspectRatio(CARD_ASPECT_RATIO)
            .clip(CardShape)
            .bounceClick(onClick = onClick),
    ) {
        PoseImage(
            imagePath = pose.imagePath,
            contentDescription = pose.title,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CardScrim),
        )
        if (isLocked) {
            PoseProBadge(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 10.dp)
                .size(HeartSize)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = HEART_FILL_ALPHA))
                .border(1.dp, Color.White.copy(alpha = HEART_BORDER_ALPHA), CircleShape)
                .click(onClick = onToggleSaved),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_pose_heart),
                contentDescription = stringResource(if (isSaved) R.string.saved_remove else R.string.saved_add),
                colorFilter = ColorFilter.tint(
                    if (isSaved) PosePinkSoft else Color.White.copy(alpha = HEART_INACTIVE_ALPHA)
                ),
                modifier = Modifier.size(HeartGlyphSize),
            )
        }
    }
}
