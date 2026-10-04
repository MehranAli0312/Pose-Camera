package com.aipose.camera.posematch.ui.screens.captureDetail.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.poseRaisedSurface
import com.aipose.camera.posematch.ui.theme.PoseRose
import com.aipose.camera.posematch.ui.theme.PoseRoseLight
import com.aipose.camera.posematch.ui.theme.PoseTextBright
import com.aipose.camera.posematch.ui.theme.PoseVioletPale
import com.aipose.camera.posematch.ui.theme.Violet
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val ButtonCorner = 20.dp
private val ButtonShape = RoundedCornerShape(ButtonCorner)
private val ButtonMinHeight = 52.dp
private val ShareGlyphSize = DpSize(17.dp, 18.dp)
private val CameraGlyphSize = DpSize(17.dp, 12.dp)
private val TrashGlyphSize = DpSize(16.dp, 18.dp)
private const val SHARE_BORDER_ALPHA = 0.1f
private const val RESHOOT_FILL_ALPHA = 0.16f
private const val RESHOOT_BORDER_ALPHA = 0.45f
private const val DELETE_FILL_ALPHA = 0.14f
private const val DELETE_BORDER_ALPHA = 0.4f
private const val SHARE_WEIGHT = 104f
private const val RESHOOT_WEIGHT = 118f
private const val DELETE_WEIGHT = 104f

@Composable
internal fun PhotoDetailActions(
    onShare: () -> Unit,
    onReshoot: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ActionButton(
            iconRes = R.drawable.ic_saved_share,
            glyphSize = ShareGlyphSize,
            label = stringResource(R.string.action_share),
            contentColor = Color.White,
            iconTint = PoseTextBright,
            onClick = onShare,
            modifier = Modifier
                .weight(SHARE_WEIGHT)
                .poseRaisedSurface(ButtonShape, borderAlpha = SHARE_BORDER_ALPHA),
        )
        ActionButton(
            iconRes = R.drawable.ic_saved_camera,
            glyphSize = CameraGlyphSize,
            label = stringResource(R.string.detail_reshoot),
            contentColor = PoseVioletPale,
            iconTint = PoseVioletPale,
            onClick = onReshoot,
            modifier = Modifier
                .weight(RESHOOT_WEIGHT)
                .tintedSurface(Violet, RESHOOT_FILL_ALPHA, RESHOOT_BORDER_ALPHA),
        )
        ActionButton(
            iconRes = R.drawable.ic_trash,
            glyphSize = TrashGlyphSize,
            label = stringResource(R.string.action_delete),
            contentColor = PoseRoseLight,
            iconTint = PoseRoseLight,
            onClick = onDelete,
            modifier = Modifier
                .weight(DELETE_WEIGHT)
                .tintedSurface(PoseRose, DELETE_FILL_ALPHA, DELETE_BORDER_ALPHA),
        )
    }
}

@Composable
private fun ActionButton(
    @DrawableRes iconRes: Int,
    glyphSize: DpSize,
    label: String,
    contentColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .heightIn(min = ButtonMinHeight)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(iconTint),
            modifier = Modifier.size(glyphSize),
        )
        Text(
            text = label,
            style = poseTextStyle(11.5.sp, FontWeight.Bold, contentColor),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun Modifier.tintedSurface(color: Color, fillAlpha: Float, borderAlpha: Float): Modifier = this
    .background(color.copy(alpha = fillAlpha), ButtonShape)
    .border(1.dp, color.copy(alpha = borderAlpha), ButtonShape)
