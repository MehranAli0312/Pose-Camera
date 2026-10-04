package com.aipose.camera.posematch.ui.screens.collections.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.PERFECT_MATCH_SCORE
import com.aipose.camera.posematch.ui.common.GlossyIconBadge
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.poseRaisedCard
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.theme.PoseDailyBottom
import com.aipose.camera.posematch.ui.theme.PoseDailyMid
import com.aipose.camera.posematch.ui.theme.PoseDailyTitle
import com.aipose.camera.posematch.ui.theme.PoseDailyTop
import com.aipose.camera.posematch.ui.theme.PoseShadow
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val CardHeight = 100.dp
private val CardCorner = 26.dp
private val BadgeSize = 40.dp
private val ArrowSize = 40.dp
private val ArrowGlyphWidth = 8.dp
private val ArrowGlyphHeight = 14.dp
private val GlossInset = 8.dp
private val GlossTop = 4.dp
private val GlossHeight = 40.dp
private const val CARD_SHADOW_ALPHA = 0.5f
private const val CARD_BORDER_ALPHA = 0.14f
private const val GLOSS_ALPHA = 0.4f
private const val GLOSS_LAYER_ALPHA = 0.3f
private const val ARROW_FILL_ALPHA = 0.16f
private const val GRADIENT_MID_STOP = 0.55f

@Composable
internal fun PerfectShotsCard(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(CardHeight)
            .poseRaisedCard(
                cornerRadius = CardCorner,
                brush = Brush.linearGradient(
                    0f to PoseDailyTop,
                    GRADIENT_MID_STOP to PoseDailyMid,
                    1f to PoseDailyBottom,
                ),
                shadowColor = PoseShadow,
                shadowAlpha = CARD_SHADOW_ALPHA,
                borderColor = Color.White.copy(alpha = CARD_BORDER_ALPHA),
            )
            .drawBehind {
                val inset = GlossInset.toPx()
                val top = GlossTop.toPx()
                val height = GlossHeight.toPx()
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = GLOSS_ALPHA), Color.Transparent),
                        startY = top,
                        endY = top + height,
                    ),
                    topLeft = Offset(inset, top),
                    size = Size(size.width - inset * 2f, height),
                    cornerRadius = CornerRadius(height / 2f),
                    alpha = GLOSS_LAYER_ALPHA,
                )
            }
            .bounceClick(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GlossyIconBadge(
            iconRes = R.drawable.ic_pose_trophy,
            palette = GlossyBadgePalette.Amber,
            size = BadgeSize,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = pluralStringResource(R.plurals.collections_perfect_title, count, count),
                style = poseTextStyle(15.sp, FontWeight.Bold, Color.White),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = pluralStringResource(
                    R.plurals.collections_perfect_subtitle,
                    count,
                    PERFECT_MATCH_SCORE,
                    count,
                ),
                style = poseTextStyle(10.5.sp, FontWeight.Normal, PoseDailyTitle),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            modifier = Modifier
                .size(ArrowSize)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = ARROW_FILL_ALPHA)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_pose_chevron_end),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(width = ArrowGlyphWidth, height = ArrowGlyphHeight),
            )
        }
    }
}
