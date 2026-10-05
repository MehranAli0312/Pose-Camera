package com.aipose.camera.posematch.ui.screens.achievements.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.BadgeStatus
import com.aipose.camera.posematch.ui.common.GlossyIconBadge
import com.aipose.camera.posematch.ui.common.poseRaisedSurface
import com.aipose.camera.posematch.ui.screens.achievements.models.style
import com.aipose.camera.posematch.ui.theme.PoseTextDim
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val TileShape = RoundedCornerShape(24.dp)
private val BadgeSize = 42.dp
private val LockedBadgeShape = RoundedCornerShape(13.5.dp)
private val LockGlyphSize = 18.dp

private const val LOCKED_FILL_ALPHA = 0.03f
private const val LOCKED_BORDER_ALPHA = 0.07f
private const val LOCKED_BADGE_ALPHA = 0.06f

@Composable
internal fun AchievementBadgeTile(
    status: BadgeStatus,
    modifier: Modifier = Modifier,
) {
    val style = status.badge.style
    val label = stringResource(style.labelRes)
    val description = if (status.isUnlocked) {
        label
    } else {
        stringResource(R.string.achievements_badge_locked, label)
    }
    val surface = if (status.isUnlocked) {
        Modifier.poseRaisedSurface(TileShape)
    } else {
        Modifier
            .clip(TileShape)
            .background(Color.White.copy(alpha = LOCKED_FILL_ALPHA))
            .border(1.dp, Color.White.copy(alpha = LOCKED_BORDER_ALPHA), TileShape)
    }
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .then(surface)
            .clearAndSetSemantics { contentDescription = description }
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        if (status.isUnlocked) {
            GlossyIconBadge(
                iconRes = style.iconRes,
                palette = style.palette,
                size = BadgeSize,
                glyphSize = style.glyphSize.coerceAtMost(BadgeSize),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(BadgeSize)
                    .background(Color.White.copy(alpha = LOCKED_BADGE_ALPHA), LockedBadgeShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_hero_lock_small),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(PoseTextDim),
                    modifier = Modifier.size(LockGlyphSize),
                )
            }
        }
        Text(
            text = label,
            style = poseTextStyle(
                9.5.sp,
                FontWeight.Bold,
                if (status.isUnlocked) Color.White else PoseTextDim,
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
