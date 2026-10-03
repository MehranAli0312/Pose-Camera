package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

private val StripItemShape = RoundedCornerShape(12.dp)

@Composable
internal fun PoseStrip(
    poses: List<Pose>,
    selectedPoseId: Int?,
    onPoseSelected: (Pose) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(top = 6.dp, bottom = 4.dp)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(poses, key = { pose -> pose.id }) { pose ->
                PoseStripItem(
                    pose = pose,
                    isSelected = pose.id == selectedPoseId,
                    onClick = { onPoseSelected(pose) },
                )
            }
        }
    }
}

@Composable
private fun PoseStripItem(
    pose: Pose,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAppPalette.current
    Box(
        modifier = modifier
            .size(width = 46.dp, height = 62.dp)
            .clip(StripItemShape)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) palette.accent else palette.glass,
                shape = StripItemShape,
            )
            .bounceClick(onClick = onClick)
    ) {
        PoseImage(
            imagePath = pose.imagePath,
            contentDescription = pose.title,
            modifier = Modifier.fillMaxSize(),
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.camera_selected),
                tint = palette.accent,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(22.dp),
            )
        }
    }
}
