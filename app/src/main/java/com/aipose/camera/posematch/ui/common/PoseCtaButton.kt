package com.aipose.camera.posematch.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.theme.poseTextStyle

val PoseCtaHeight = 58.dp
private val CtaShadowInset = 2.dp
private val CtaShadowOffset = 6.dp
private val CtaGlossInset = 8.dp
private val CtaGlossTop = 4.dp
private val CtaIconWidth = 8.dp
private val CtaIconHeight = 14.dp
private val CtaLabelSize = 16.sp
private val CtaLabelGap = 10.dp
private const val CTA_SHADOW_ALPHA = 0.5f
private const val CTA_GLOSS_ALPHA = 0.42f
private const val CTA_GLOSS_HEIGHT_RATIO = 0.4655f
private const val GRADIENT_MID_STOP = 0.5f
private const val GRADIENT_END_X_RATIO = 0.0438f
private const val GRADIENT_END_Y_RATIO = 1.7706f

@Composable
fun PoseCtaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes trailingIconRes: Int? = null,
    palette: GlossyBadgePalette = GlossyBadgePalette.HeroCta,
) {
    val shape = RoundedCornerShape(PoseCtaHeight / 2f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(PoseCtaHeight)
            .drawBehind { drawCtaShadow(palette) }
            .clip(shape)
            .drawBehind { drawCtaSurface(palette) }
            .bounceClick(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CtaLabelGap, Alignment.CenterHorizontally),
    ) {
        Text(
            text = text,
            style = poseTextStyle(CtaLabelSize, FontWeight.Bold, Color.White),
        )
        if (trailingIconRes != null) {
            Icon(
                painter = painterResource(trailingIconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(width = CtaIconWidth, height = CtaIconHeight),
            )
        }
    }
}

private fun DrawScope.drawCtaShadow(palette: GlossyBadgePalette) {
    val inset = CtaShadowInset.toPx()
    drawRoundRect(
        color = palette.shadow.copy(alpha = CTA_SHADOW_ALPHA),
        topLeft = Offset(inset, CtaShadowOffset.toPx()),
        size = Size(size.width - inset * 2f, size.height),
        cornerRadius = CornerRadius(size.height / 2f),
    )
}

private fun DrawScope.drawCtaSurface(palette: GlossyBadgePalette) {
    drawRect(
        brush = Brush.linearGradient(
            0f to palette.top,
            GRADIENT_MID_STOP to palette.mid,
            1f to palette.bottom,
            start = Offset.Zero,
            end = Offset(
                size.width * GRADIENT_END_X_RATIO,
                size.height * GRADIENT_END_Y_RATIO,
            ),
        )
    )
    val inset = CtaGlossInset.toPx()
    val top = CtaGlossTop.toPx()
    val glossHeight = size.height * CTA_GLOSS_HEIGHT_RATIO
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = CTA_GLOSS_ALPHA), Color.Transparent),
            startY = top,
            endY = top + glossHeight,
        ),
        topLeft = Offset(inset, top),
        size = Size(size.width - inset * 2f, glossHeight),
        cornerRadius = CornerRadius(glossHeight / 2f),
    )
}
