package com.aipose.camera.posematch.ui.screens.saved.components

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
import com.aipose.camera.posematch.domain.models.PERFECT_MATCH_SCORE
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.click
import com.aipose.camera.posematch.ui.screens.saved.models.SavedShot
import com.aipose.camera.posematch.ui.theme.PoseCardLabel
import com.aipose.camera.posematch.ui.theme.PosePinkSoft
import com.aipose.camera.posematch.ui.theme.PoseScoreHigh
import com.aipose.camera.posematch.ui.theme.PoseScoreLow
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import com.aipose.camera.posematch.util.bidiIsolate

private val CardShape = RoundedCornerShape(22.dp)
private val HeartButtonSize = 28.dp
private val HeartGlyphSize = 12.dp
private val ScorePillShape = RoundedCornerShape(11.dp)
private val ScoreDotSize = 6.4.dp

private const val CARD_ASPECT_RATIO = 165f / 210f
private const val SCRIM_MID_STOP = 0.48f
private const val SCRIM_MID_ALPHA = 0.1f
private const val SCRIM_END_ALPHA = 0.82f
private const val HEART_FILL_ALPHA = 0.45f
private const val HEART_BORDER_ALPHA = 0.22f
private const val SCORE_PILL_ALPHA = 0.52f

@Composable
internal fun SavedShotCard(
    shot: SavedShot,
    onClick: () -> Unit,
    onUnsave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(CARD_ASPECT_RATIO)
            .clip(CardShape)
            .bounceClick(onClick = onClick),
    ) {
        PoseImage(
            imagePath = shot.capture.imagePath,
            contentDescription = shot.capture.title,
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
        HeartButton(
            onClick = onUnsave,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 11.dp),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        ) {
            ScorePill(score = shot.capture.matchScore)
            Text(
                text = shot.locationLabel,
                style = poseTextStyle(10.sp, FontWeight.Bold, PoseCardLabel),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun HeartButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(HeartButtonSize)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = HEART_FILL_ALPHA))
            .border(1.dp, Color.White.copy(alpha = HEART_BORDER_ALPHA), CircleShape)
            .click(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_pose_heart),
            contentDescription = stringResource(R.string.saved_remove),
            colorFilter = ColorFilter.tint(PosePinkSoft),
            modifier = Modifier.size(HeartGlyphSize),
        )
    }
}

@Composable
private fun ScorePill(score: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(ScorePillShape)
            .background(Color.Black.copy(alpha = SCORE_PILL_ALPHA))
            .padding(horizontal = 11.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(ScoreDotSize)
                .clip(CircleShape)
                .background(if (score >= PERFECT_MATCH_SCORE) PoseScoreHigh else PoseScoreLow),
        )
        Text(
            text = stringResource(R.string.score_percent, score).bidiIsolate(),
            style = poseTextStyle(9.5.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
        )
    }
}
