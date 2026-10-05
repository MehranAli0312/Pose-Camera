package com.aipose.camera.posematch.ui.screens.achievements.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.StreakDay
import com.aipose.camera.posematch.ui.common.GlossyIconBadge
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.theme.PoseAchievementStreakBottom
import com.aipose.camera.posematch.ui.theme.PoseAchievementStreakMid
import com.aipose.camera.posematch.ui.theme.PoseAchievementStreakTop
import com.aipose.camera.posematch.ui.theme.PoseShadow
import com.aipose.camera.posematch.ui.theme.PoseStreakSand
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val CardCorner = 26.dp
private val CardShape = RoundedCornerShape(CardCorner)
private val CardMinHeight = 170.dp
private val FlameBadgeSize = 60.dp
private val FlameGlyphSize = 30.dp
private val ShadowInset = 2.dp
private val ShadowOffset = 6.dp
private val GlossInset = 8.dp
private val GlossTop = 4.dp
private val GlossHeight = 40.dp

private const val CARD_BORDER_ALPHA = 0.14f
private const val CARD_SHADOW_ALPHA = 0.5f
private const val GLOSS_ALPHA = 0.42f
private const val GLOSS_LAYER_ALPHA = 0.22f
private const val GRADIENT_MID_STOP = 0.55f

@Composable
internal fun AchievementsStreakCard(
    currentStreak: Int,
    bestStreak: Int,
    week: List<StreakDay>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CardMinHeight)
            .drawBehind { drawCardShadow() }
            .clip(CardShape)
            .drawBehind { drawCardSurface() }
            .border(1.dp, Color.White.copy(alpha = CARD_BORDER_ALPHA), CardShape)
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlossyIconBadge(
                iconRes = R.drawable.ic_pose_flame,
                palette = GlossyBadgePalette.Orange,
                size = FlameBadgeSize,
                glyphSize = FlameGlyphSize,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pluralStringResource(
                        R.plurals.achievements_streak_title,
                        currentStreak,
                        currentStreak,
                    ),
                    style = poseTextStyle(24.sp, FontWeight.Bold, Color.White),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = pluralStringResource(
                        R.plurals.achievements_personal_best,
                        bestStreak,
                        bestStreak,
                    ),
                    style = poseTextStyle(11.5.sp, FontWeight.Normal, PoseStreakSand),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        StreakWeekRow(days = week)
    }
}

private fun DrawScope.drawCardShadow() {
    val inset = ShadowInset.toPx()
    drawRoundRect(
        color = PoseShadow.copy(alpha = CARD_SHADOW_ALPHA),
        topLeft = Offset(inset, ShadowOffset.toPx()),
        size = Size(size.width - inset * 2f, size.height - inset),
        cornerRadius = CornerRadius(CardCorner.toPx()),
    )
}

private fun DrawScope.drawCardSurface() {
    drawRect(
        brush = Brush.linearGradient(
            0f to PoseAchievementStreakTop,
            GRADIENT_MID_STOP to PoseAchievementStreakMid,
            1f to PoseAchievementStreakBottom,
            start = Offset.Zero,
            end = Offset(size.width, size.height),
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
        cornerRadius = CornerRadius(glossHeight / 2f),
        alpha = GLOSS_LAYER_ALPHA,
    )
}
