package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.PosePremiumGold
import com.aipose.camera.posematch.ui.theme.PoseVioletLight

private val ItemShape = RoundedCornerShape(12.dp)
private val ItemSpacing = 2.dp
private val ItemWidth = 56.dp
private val ItemHeight = 72.dp
private val BadgeSize = 18.dp
private val BadgeGlyphSize = 9.dp
private val LockGlyphSize = 10.dp
private const val BORDER_ALPHA = 0.14f
private const val LOCK_FILL_ALPHA = 0.6f

@Composable
internal fun PoseStrip(
    poses: List<Pose>,
    selectedPoseId: Int?,
    lockedPoseIds: Set<Int>,
    onPoseSelected: (Pose) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 36.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(poses, key = { pose -> pose.id }) { pose ->
            PoseStripItem(
                pose = pose,
                isSelected = pose.id == selectedPoseId,
                isLocked = pose.id in lockedPoseIds,
                onClick = { onPoseSelected(pose) },
                modifier = Modifier.padding(horizontal = ItemSpacing),
            )
        }
    }
}

@Composable
private fun PoseStripItem(
    pose: Pose,
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = ItemWidth, height = ItemHeight)
            .clip(ItemShape)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) {
                    PoseVioletLight
                } else {
                    Color.White.copy(alpha = BORDER_ALPHA)
                },
                shape = ItemShape,
            )
            .bounceClick(onClick = onClick)
    ) {
        PoseImage(
            imagePath = pose.imagePath,
            contentDescription = pose.title,
            modifier = Modifier.fillMaxSize(),
        )
        if (isLocked) {
            PoseStripLockBadge(modifier = Modifier.align(Alignment.TopStart))
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(BadgeSize)
                    .clip(CircleShape)
                    .background(PoseVioletLight),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_pose_check_small),
                    contentDescription = stringResource(R.string.camera_selected),
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(BadgeGlyphSize),
                )
            }
        }
    }
}

@Composable
private fun PoseStripLockBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .size(BadgeSize)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = LOCK_FILL_ALPHA)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_hero_lock_small),
            contentDescription = stringResource(R.string.pose_pro_badge),
            colorFilter = ColorFilter.tint(PosePremiumGold),
            modifier = Modifier.size(LockGlyphSize),
        )
    }
}
