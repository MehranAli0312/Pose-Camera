package com.aipose.camera.posematch.ui.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.GlossyIconBadge
import com.aipose.camera.posematch.ui.common.click
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.theme.PoseShadow
import com.aipose.camera.posematch.ui.theme.PoseStreakCardBottom
import com.aipose.camera.posematch.ui.theme.PoseStreakCardMid
import com.aipose.camera.posematch.ui.theme.PoseStreakCardTop
import com.aipose.camera.posematch.ui.theme.PoseTextLavender
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val CardHeight = 96.dp
private val CardCorner = 26.dp
private val CardShape = RoundedCornerShape(CardCorner)
private val CardPadding = 16.dp
private val TrophySize = 48.dp
private val TrophyToText = 16.dp
private val ChevronCircleSize = 36.dp
private val ChevronWidth = 7.dp
private val ChevronHeight = 12.dp
private val TitleSize = 16.sp
private val SubtitleSize = 11.sp
private val ShadowInset = 2.dp
private val ShadowOffset = 6.dp
private val GlossInset = 8.dp
private val GlossTop = 4.dp
private val GlossHeight = 38.dp
private val GlossCorner = 19.dp

private const val CARD_BORDER_ALPHA = 0.14f
private const val CARD_SHADOW_ALPHA = 0.5f
private const val GLOSS_ALPHA = 0.42f
private const val GLOSS_LAYER_ALPHA = 0.26f
private const val CHEVRON_CIRCLE_ALPHA = 0.14f
private const val GRADIENT_MID_STOP = 0.55f
private const val GRADIENT_END_X_RATIO = 0.14f
private const val GRADIENT_END_Y_RATIO = 1.86f

@Composable
internal fun SettingsStreakCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(CardHeight)
            .drawBehind { drawStreakShadow() }
            .clip(CardShape)
            .drawBehind { drawStreakSurface() }
            .border(1.dp, Color.White.copy(alpha = CARD_BORDER_ALPHA), CardShape)
            .click(onClick = onClick)
            .padding(horizontal = CardPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlossyIconBadge(
            iconRes = R.drawable.ic_pose_trophy,
            palette = GlossyBadgePalette.Amber,
            size = TrophySize,
        )
        Spacer(modifier = Modifier.width(TrophyToText))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = poseTextStyle(TitleSize, FontWeight.Bold, Color.White),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = poseTextStyle(SubtitleSize, FontWeight.Normal, PoseTextLavender),
            )
        }
        Box(
            modifier = Modifier
                .size(ChevronCircleSize)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = CHEVRON_CIRCLE_ALPHA)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_pose_chevron_end),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(width = ChevronWidth, height = ChevronHeight),
            )
        }
    }
}

private fun DrawScope.drawStreakShadow() {
    val inset = ShadowInset.toPx()
    drawRoundRect(
        color = PoseShadow.copy(alpha = CARD_SHADOW_ALPHA),
        topLeft = Offset(inset, ShadowOffset.toPx()),
        size = Size(size.width - inset * 2f, size.height - inset),
        cornerRadius = CornerRadius(CardCorner.toPx()),
    )
}

private fun DrawScope.drawStreakSurface() {
    drawRect(
        brush = Brush.linearGradient(
            0f to PoseStreakCardTop,
            GRADIENT_MID_STOP to PoseStreakCardMid,
            1f to PoseStreakCardBottom,
            start = Offset.Zero,
            end = Offset(
                size.width * GRADIENT_END_X_RATIO,
                size.height * GRADIENT_END_Y_RATIO,
            ),
        )
    )
    val inset = GlossInset.toPx()
    val top = GlossTop.toPx()
    val glossHeight = GlossHeight.toPx()
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = GLOSS_ALPHA), Color.Transparent),
            startY = top,
            endY = top + glossHeight,
        ),
        topLeft = Offset(inset, top),
        size = Size(size.width - inset * 2f, glossHeight),
        cornerRadius = CornerRadius(GlossCorner.toPx()),
        alpha = GLOSS_LAYER_ALPHA,
    )
}
