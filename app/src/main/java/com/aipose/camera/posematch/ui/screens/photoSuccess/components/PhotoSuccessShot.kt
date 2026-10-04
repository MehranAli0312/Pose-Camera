package com.aipose.camera.posematch.ui.screens.photoSuccess.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.theme.Emerald
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseEmeraldLight
import com.aipose.camera.posematch.ui.theme.PoseShadow
import com.aipose.camera.posematch.ui.theme.Violet
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val ShotWidth = 200.dp
private val ShotHeight = 250.dp
private val ShotShape = RoundedCornerShape(24.dp)
private val ShotElevation = 16.dp
private val ChipHeight = 30.dp
private val ChipShape = RoundedCornerShape(15.dp)
private val ChipCheckSize = 10.dp
private val PinWidth = 13.dp
private val PinHeight = 17.dp
private const val CHIP_BACKGROUND_ALPHA = 0.18f
private const val CHIP_BORDER_ALPHA = 0.45f

@Composable
internal fun PhotoSuccessShot(
    imagePath: String,
    matchScore: Int?,
    placeLabel: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PoseImage(
            imagePath = imagePath,
            contentDescription = stringResource(R.string.saved_photo),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = ShotWidth, height = ShotHeight)
                .shadow(ShotElevation, ShotShape, ambientColor = PoseShadow, spotColor = PoseShadow)
                .clip(ShotShape),
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (matchScore != null) {
            MatchChip(score = matchScore)
            Spacer(modifier = Modifier.height(16.dp))
        }
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_saved_pin),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Violet),
                modifier = Modifier.size(width = PinWidth, height = PinHeight),
            )
            Text(
                text = placeLabel,
                style = poseTextStyle(11.sp, FontWeight.Bold, LocalAppPalette.current.textMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MatchChip(
    score: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(ChipHeight)
            .clip(ChipShape)
            .background(Emerald.copy(alpha = CHIP_BACKGROUND_ALPHA))
            .border(1.dp, Emerald.copy(alpha = CHIP_BORDER_ALPHA), ChipShape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_pose_check_small),
            contentDescription = null,
            colorFilter = ColorFilter.tint(PoseEmeraldLight),
            modifier = Modifier.size(ChipCheckSize),
        )
        Text(
            text = stringResource(R.string.edit_match_badge, score),
            style = poseTextStyle(10.5.sp, FontWeight.Bold, PoseEmeraldLight),
            maxLines = 1,
        )
    }
}
