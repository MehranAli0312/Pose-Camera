package com.aipose.camera.posematch.ui.screens.bottomSheet.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.PoseGlowIndigo
import com.aipose.camera.posematch.ui.theme.PosePremiumCtaDeep
import com.aipose.camera.posematch.ui.theme.PosePremiumGold
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val ButtonHeight = 56.dp
private val ButtonShape = RoundedCornerShape(28.dp)
private val GlyphCircleSize = 22.dp
private val PlayGlyphSize = 11.dp
private val CrownGlyphSize = 20.dp
private val ProgressSize = 22.dp
private val LabelSize = 16.sp
private val ProgressStroke = 2.5.dp
private const val PLAY_RING_ALPHA = 0.9f
private const val DISABLED_ALPHA = 0.5f

@Composable
internal fun WatchAdToUnlockButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonHeight)
            .clip(ButtonShape)
            .background(Brush.verticalGradient(listOf(PoseGlowIndigo, PosePremiumCtaDeep)))
            .bounceClick(enabled = !isLoading, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(ProgressSize),
                color = Color.White,
                strokeWidth = ProgressStroke,
            )
            Text(
                text = stringResource(R.string.premium_feature_free_access_loading),
                style = poseTextStyle(LabelSize, FontWeight.Bold, Color.White),
                maxLines = 1,
            )
        } else {
            PlayGlyph()
            Text(
                text = stringResource(R.string.premium_pose_watch_ad),
                style = poseTextStyle(LabelSize, FontWeight.Bold, Color.White),
                maxLines = 1,
            )
        }
    }
}

@Composable
internal fun GoPremiumButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonHeight)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clip(ButtonShape)
            .border(1.5.dp, PosePremiumGold, ButtonShape)
            .bounceClick(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_pro_glyph_crown),
            contentDescription = null,
            tint = PosePremiumGold,
            modifier = Modifier.size(CrownGlyphSize),
        )
        Text(
            text = stringResource(R.string.premium_feature_go_premium_title),
            style = poseTextStyle(LabelSize, FontWeight.Bold, PosePremiumGold),
            maxLines = 1,
        )
    }
}

@Composable
private fun PlayGlyph(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(GlyphCircleSize)
            .border(1.5.dp, Color.White.copy(alpha = PLAY_RING_ALPHA), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_pose_play),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(PlayGlyphSize),
        )
    }
}
