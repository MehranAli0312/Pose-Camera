package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.PoseTextLavender
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val ItemShape = RoundedCornerShape(16.dp)
private val ItemWidth = 56.dp
private val ItemHeight = 72.dp
private val BadgeSize = 18.dp
private val BadgeGlyphSize = 9.dp
private val ImportGlyphSize = 20.dp
private const val SCRIM_ALPHA = 0.45f
private const val BORDER_ALPHA = 0.14f

@Composable
internal fun PoseStrip(
    poses: List<Pose>,
    selectedPoseId: Int?,
    onPoseSelected: (Pose) -> Unit,
    onImport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 34.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item(key = IMPORT_ITEM_KEY) {
            ImportTile(onClick = onImport)
        }
        items(poses, key = { pose -> pose.id }) { pose ->
            PoseStripItem(
                pose = pose,
                isSelected = pose.id == selectedPoseId,
                onClick = { onPoseSelected(pose) },
            )
        }
    }
}

@Composable
private fun ImportTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .size(width = ItemWidth, height = ItemHeight)
            .clip(ItemShape)
            .background(Color.Black.copy(alpha = SCRIM_ALPHA))
            .border(1.dp, Color.White.copy(alpha = BORDER_ALPHA), ItemShape)
            .bounceClick(onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_pose_import),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier.size(ImportGlyphSize),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.action_import),
            style = poseTextStyle(8.sp, FontWeight.Bold, PoseTextLavender),
        )
    }
}

@Composable
private fun PoseStripItem(
    pose: Pose,
    isSelected: Boolean,
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

private const val IMPORT_ITEM_KEY = "pose_strip_import"
