package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
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
import com.aipose.camera.posematch.ui.theme.PoseEmerald400
import com.aipose.camera.posematch.ui.theme.PoseEmerald600
import com.aipose.camera.posematch.ui.theme.PoseTextBright
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val BadgeHeight = 28.dp
private val BadgeShape = RoundedCornerShape(14.dp)
private val CompareHeight = 30.dp
private val CompareShape = RoundedCornerShape(15.dp)
private val CheckGlyphSize = 10.dp
private val CompareGlyphSize = 12.dp
private val LookDotSize = 8.dp
private val LookChipMaxWidth = 160.dp
private const val SCRIM_ALPHA = 0.48f
private const val COMPARE_SCRIM_ALPHA = 0.52f
private const val BORDER_ALPHA = 0.18f

@Composable
internal fun EditMatchBadge(
    score: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(BadgeHeight)
            .clip(BadgeShape)
            .background(Brush.linearGradient(listOf(PoseEmerald400, PoseEmerald600)))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_pose_check_small),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier.size(CheckGlyphSize),
        )
        Text(
            text = stringResource(R.string.edit_match_badge, score),
            style = poseTextStyle(9.5.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
        )
    }
}

@Composable
internal fun EditLookChip(
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .widthIn(max = LookChipMaxWidth)
            .height(BadgeHeight)
            .clip(BadgeShape)
            .background(Color.Black.copy(alpha = SCRIM_ALPHA))
            .border(1.dp, Color.White.copy(alpha = BORDER_ALPHA), BadgeShape)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(LookDotSize)
                .clip(CircleShape)
                .background(PoseVioletLight)
        )
        Text(
            text = label,
            style = poseTextStyle(9.5.sp, FontWeight.Bold, PoseTextBright),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun EditCompareChip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(CompareHeight)
            .clip(CompareShape)
            .background(Color.Black.copy(alpha = COMPARE_SCRIM_ALPHA))
            .border(1.dp, Color.White.copy(alpha = BORDER_ALPHA), CompareShape)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_edit_compare),
            contentDescription = null,
            colorFilter = ColorFilter.tint(PoseVioletLight),
            modifier = Modifier.size(CompareGlyphSize),
        )
        Text(
            text = stringResource(R.string.edit_compare),
            style = poseTextStyle(9.5.sp, FontWeight.Bold, PoseTextBright),
            maxLines = 1,
        )
    }
}
