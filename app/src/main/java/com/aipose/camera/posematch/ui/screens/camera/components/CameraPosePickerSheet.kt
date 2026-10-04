package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.ui.common.PoseThumbnail
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val SheetMaxHeight = 420.dp
private const val GRID_COLUMNS = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CameraPosePickerSheet(
    poses: List<Pose>,
    selectedPoseId: Int?,
    onPoseSelected: (Pose) -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = LocalAppPalette.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = palette.backgroundMid,
        dragHandle = null,
    ) {
        Text(
            text = stringResource(R.string.camera_change_pose),
            style = poseTextStyle(15.sp, FontWeight.Bold, Color.White),
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 12.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_COLUMNS),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = SheetMaxHeight),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(poses, key = { pose -> pose.id }) { pose ->
                PoseThumbnail(
                    pose = pose,
                    isSaved = pose.id == selectedPoseId,
                    onClick = { onPoseSelected(pose) },
                )
            }
        }
    }
}
