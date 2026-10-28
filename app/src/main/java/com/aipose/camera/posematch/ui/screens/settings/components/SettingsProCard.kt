package com.aipose.camera.posematch.ui.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.click
import com.aipose.camera.posematch.ui.theme.PosePremiumGold
import com.aipose.camera.posematch.ui.theme.PoseProCardEnd
import com.aipose.camera.posematch.ui.theme.PoseProCardInk
import com.aipose.camera.posematch.ui.theme.PoseProCardStart
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val CardShape = RoundedCornerShape(22.dp)
private val CardPadding = 18.dp
private val GlowRadius = 92.dp
private val GlowInsetEnd = 34.dp
private val GlowInsetTop = 8.dp
private val SparkleSize = 14.dp
private val SparkleInsetEnd = 92.dp
private val SparkleInsetTop = 26.dp
private val PillCrownSize = 14.dp
private val ArrowButtonSize = 46.dp
private val ArrowIconSize = 20.dp
private val SubtitleSparkleSize = 10.dp
private val PillLabelSize = 12.sp
private val TitleSize = 18.sp
private val SubtitleSize = 12.5.sp

private const val CARD_BORDER_ALPHA = 0.08f
private const val GLOW_ALPHA = 0.1f
private const val SPARKLE_ALPHA = 0.9f
private const val SUBTITLE_ALPHA = 0.8f

@Composable
internal fun SettingsProCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(Brush.horizontalGradient(listOf(PoseProCardStart, PoseProCardEnd)))
            .proCardGlow()
            .border(1.dp, Color.White.copy(alpha = CARD_BORDER_ALPHA), CardShape)
            .semantics { role = Role.Button }
            .click(onClick = onClick),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_pose_sparkle),
            contentDescription = null,
            tint = Color.White.copy(alpha = SPARKLE_ALPHA),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = SparkleInsetTop, end = SparkleInsetEnd)
                .size(SparkleSize),
        )
        Row(
            modifier = Modifier.padding(CardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ProPill()
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.settings_pro_card_title),
                    style = poseTextStyle(TitleSize, FontWeight.Bold, Color.White),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                ProCardSubtitle()
            }
            Spacer(modifier = Modifier.width(14.dp))
            ProArrowButton()
        }
    }
}

@Composable
private fun ProPill() {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_pro_glyph_crown),
            contentDescription = null,
            tint = PosePremiumGold,
            modifier = Modifier.size(PillCrownSize),
        )
        Text(
            text = stringResource(R.string.pose_pro_badge),
            style = poseTextStyle(PillLabelSize, FontWeight.Bold, PoseProCardInk),
            maxLines = 1,
        )
    }
}

@Composable
private fun ProCardSubtitle() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.settings_pro_card_subtitle),
            style = poseTextStyle(SubtitleSize, FontWeight.Medium, Color.White.copy(alpha = SUBTITLE_ALPHA)),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Spacer(modifier = Modifier.width(5.dp))
        Icon(
            painter = painterResource(R.drawable.ic_pose_sparkle),
            contentDescription = null,
            tint = PosePremiumGold,
            modifier = Modifier.size(SubtitleSparkleSize),
        )
    }
}

@Composable
private fun ProArrowButton() {
    Box(
        modifier = Modifier
            .size(ArrowButtonSize)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_pose_arrow_end),
            contentDescription = null,
            tint = PoseProCardEnd,
            modifier = Modifier.size(ArrowIconSize),
        )
    }
}

@Composable
private fun Modifier.proCardGlow(): Modifier {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    return drawBehind {
        val insetEnd = GlowInsetEnd.toPx()
        val centerX = if (isRtl) insetEnd else size.width - insetEnd
        drawCircle(
            color = Color.White.copy(alpha = GLOW_ALPHA),
            radius = GlowRadius.toPx(),
            center = Offset(centerX, GlowInsetTop.toPx()),
        )
    }
}
