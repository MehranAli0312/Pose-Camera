package com.aipose.camera.posematch.ui.screens.bottomSheet.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.common.PoseProBadge
import com.aipose.camera.posematch.ui.theme.PosePremiumGold
import com.aipose.camera.posematch.ui.theme.PosePremiumGoldDeep
import com.aipose.camera.posematch.ui.theme.PosePremiumGoldLight

private val PreviewWidth = 128.dp
private val PreviewHeight = 178.dp
private val PreviewShape = RoundedCornerShape(20.dp)
private val LockBadgeSize = 48.dp
private val LockBadgeShape = RoundedCornerShape(16.dp)
private val LockGlyphSize = 26.dp
private const val PREVIEW_SCRIM_ALPHA = 0.38f
private const val PREVIEW_BORDER_ALPHA = 0.85f
private const val LOCK_GLOW_ALPHA = 0.5f
private const val LOCK_GLOW_SCALE = 1.7f

@Composable
internal fun PremiumPosePreview(
    imagePath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(PreviewWidth)
            .height(PreviewHeight)
            .clip(PreviewShape)
            .border(1.5.dp, PosePremiumGold.copy(alpha = PREVIEW_BORDER_ALPHA), PreviewShape),
    ) {
        PoseImage(
            imagePath = imagePath,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = PREVIEW_SCRIM_ALPHA)),
        )
        PoseProBadge(
            withLock = false,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
        )
        LockBadge(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun LockBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(LockBadgeSize)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PosePremiumGold.copy(alpha = LOCK_GLOW_ALPHA),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = size.minDimension / 2f * LOCK_GLOW_SCALE,
                    ),
                    radius = size.minDimension / 2f * LOCK_GLOW_SCALE,
                )
            }
            .background(
                brush = Brush.verticalGradient(
                    listOf(PosePremiumGoldLight, PosePremiumGoldDeep),
                ),
                shape = LockBadgeShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_hero_lock_small),
            contentDescription = stringResource(R.string.pose_locked),
            tint = Color.White,
            modifier = Modifier.size(LockGlyphSize),
        )
    }
}
