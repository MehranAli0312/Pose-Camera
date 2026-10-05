package com.aipose.camera.posematch.ui.screens.captureDetail.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseScreenGutter
import com.aipose.camera.posematch.ui.common.PoseScreenTopSpacing
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.PosePinkSoft

private val ButtonSize = 40.dp
private val BackGlyphSize = DpSize(10.dp, 17.dp)
private val HeartGlyphSize = DpSize(16.dp, 15.dp)
private val GlossInset = 3.dp
private const val SCRIM_ALPHA = 0.42f
private const val BORDER_ALPHA = 0.2f
private const val GLOSS_ALPHA = 0.42f
private const val GLOSS_LAYER_ALPHA = 0.45f
private const val GLOSS_HEIGHT_RATIO = 0.42f

@Composable
internal fun PhotoDetailTopBar(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = PoseScreenGutter, end = PoseScreenGutter, top = PoseScreenTopSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlassButton(
            iconRes = R.drawable.ic_pose_back,
            glyphSize = BackGlyphSize,
            tint = Color.White,
            contentDescription = stringResource(R.string.action_back),
            onClick = onBack,
        )
        Spacer(modifier = Modifier.weight(1f))
        GlassButton(
            iconRes = R.drawable.ic_pose_heart,
            glyphSize = HeartGlyphSize,
            tint = if (isFavorite) PosePinkSoft else Color.White,
            contentDescription = stringResource(if (isFavorite) R.string.saved_remove else R.string.saved_add),
            onClick = onToggleFavorite,
        )
    }
}

@Composable
private fun GlassButton(
    @DrawableRes iconRes: Int,
    glyphSize: DpSize,
    tint: Color,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(ButtonSize)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = SCRIM_ALPHA))
            .drawBehind {
                val inset = GlossInset.toPx()
                val height = size.height * GLOSS_HEIGHT_RATIO
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = GLOSS_ALPHA), Color.Transparent),
                        startY = inset,
                        endY = inset + height,
                    ),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2f, height),
                    cornerRadius = CornerRadius(height / 2f),
                    alpha = GLOSS_LAYER_ALPHA,
                )
            }
            .border(1.dp, Color.White.copy(alpha = BORDER_ALPHA), CircleShape)
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
