package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.poseGradientPill
import com.aipose.camera.posematch.ui.common.poseRaisedSurface
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.theme.PoseTextBright
import com.aipose.camera.posematch.ui.theme.PoseTextLavender
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val BarHeight = 56.dp
private val CloseSize = 40.dp
private val ResetSize = 36.dp
private val CloseGlyphSize = 18.dp
private val ResetGlyphSize = 17.dp
private val SaveMinWidth = 84.dp
private val SaveHeight = 36.dp
private val SaveCorner = 18.dp
private val SaveGlossHeight = 17.dp
private val SaveProgressSize = 16.dp
private val SaveProgressStroke = 2.dp
private const val GLASS_BORDER_ALPHA = 0.12f

@Composable
internal fun PhotoEditTopBar(
    isSaving: Boolean,
    onDiscard: () -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(BarHeight)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlassCircleButton(
            iconRes = R.drawable.ic_close,
            contentDescription = stringResource(R.string.action_discard),
            size = CloseSize,
            glyphSize = CloseGlyphSize,
            tint = PoseTextBright,
            onClick = onDiscard,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.title_edit_photo),
            style = poseTextStyle(16.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        GlassCircleButton(
            iconRes = R.drawable.ic_edit_reset,
            contentDescription = stringResource(R.string.action_reset),
            size = ResetSize,
            glyphSize = ResetGlyphSize,
            tint = PoseTextLavender,
            onClick = onReset,
        )
        Spacer(modifier = Modifier.width(10.dp))
        SaveButton(isSaving = isSaving, onClick = onSave)
    }
}

@Composable
private fun SaveButton(
    isSaving: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .widthIn(min = SaveMinWidth)
            .height(SaveHeight)
            .poseGradientPill(
                palette = GlossyBadgePalette.HeroCta,
                cornerRadius = SaveCorner,
                glossInsetX = 6.dp,
                glossTop = 3.dp,
                glossHeight = SaveGlossHeight,
            )
            .bounceClick(enabled = !isSaving, onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = SaveProgressStroke,
                modifier = Modifier.size(SaveProgressSize),
            )
        } else {
            Text(
                text = stringResource(R.string.action_save),
                style = poseTextStyle(14.sp, FontWeight.Bold, Color.White),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun GlassCircleButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    size: Dp,
    glyphSize: Dp,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .poseRaisedSurface(CircleShape, borderAlpha = GLASS_BORDER_ALPHA)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(tint),
            modifier = Modifier.size(glyphSize),
        )
    }
}
