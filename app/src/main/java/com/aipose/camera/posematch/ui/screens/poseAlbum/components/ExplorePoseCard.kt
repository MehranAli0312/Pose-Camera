package com.aipose.camera.posematch.ui.screens.poseAlbum.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.click
import com.aipose.camera.posematch.ui.screens.poseAlbum.models.PoseDifficulty
import com.aipose.camera.posematch.ui.theme.PoseCardLabel
import com.aipose.camera.posematch.ui.theme.PosePinkSoft
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import java.util.Locale

private val CardShape = RoundedCornerShape(22.dp)
private val TagShape = RoundedCornerShape(12.dp)
private val HeartSize = 26.dp
private val HeartGlyphSize = 12.dp
private val DotSize = 6.8.dp
private val TagMaxWidth = 110.dp
private const val CARD_ASPECT_RATIO = 165f / 190f
private const val SCRIM_MID_STOP = 0.45f
private const val SCRIM_MID_ALPHA = 0.08f
private const val SCRIM_END_ALPHA = 0.85f
private const val TAG_FILL_ALPHA = 0.48f
private const val TAG_BORDER_ALPHA = 0.2f
private const val HEART_FILL_ALPHA = 0.45f
private const val HEART_BORDER_ALPHA = 0.22f
private const val HEART_INACTIVE_ALPHA = 0.9f

@Composable
internal fun ExplorePoseCard(
    pose: Pose,
    isSaved: Boolean,
    onClick: () -> Unit,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
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
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        SCRIM_MID_STOP to Color.Black.copy(alpha = SCRIM_MID_ALPHA),
                        1f to Color.Black.copy(alpha = SCRIM_END_ALPHA),
                    )
                ),
        )
        Text(
            text = pose.category.uppercase(Locale.getDefault()),
            style = poseTextStyle(9.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .widthIn(max = TagMaxWidth)
                .clip(TagShape)
                .background(Color.Black.copy(alpha = TAG_FILL_ALPHA))
                .border(1.dp, Color.White.copy(alpha = TAG_BORDER_ALPHA), TagShape)
                .padding(horizontal = 15.dp, vertical = 6.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 11.dp, end = 12.dp)
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
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                text = pose.title,
                style = poseTextStyle(13.sp, FontWeight.Bold, Color.White),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                PoseDifficulty.fromKey(pose.difficulty)?.let { difficulty ->
                    Box(
                        modifier = Modifier
                            .size(DotSize)
                            .clip(CircleShape)
                            .background(difficulty.dotColor)
                    )
                }
                Text(
                    text = pose.difficulty,
                    style = poseTextStyle(9.5.sp, FontWeight.Normal, PoseCardLabel),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
