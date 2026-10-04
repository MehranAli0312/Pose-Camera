package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.domain.models.Pose

const val POSE_ROW_SIZE = 3

@Composable
fun PoseThumbRow(
    poses: List<Pose>,
    onPoseClick: (Pose) -> Unit,
    modifier: Modifier = Modifier,
    savedPoseIds: Set<Int> = emptySet(),
    onToggleSaved: ((Pose) -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        poses.forEach { pose ->
            PoseThumbnail(
                pose = pose,
                onClick = { onPoseClick(pose) },
                modifier = Modifier.weight(1f),
                isSaved = pose.id in savedPoseIds,
                onToggleSaved = onToggleSaved?.let { toggle -> { toggle(pose) } },
            )
        }
        repeat(POSE_ROW_SIZE - poses.size) { Spacer(modifier = Modifier.weight(1f)) }
    }
}
