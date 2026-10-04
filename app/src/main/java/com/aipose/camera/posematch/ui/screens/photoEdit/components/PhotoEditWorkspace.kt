package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.PhotoSize
import com.aipose.camera.posematch.ui.screens.photoEdit.models.PhotoEditTab
import com.aipose.camera.posematch.ui.screens.photoEdit.models.PhotoEditUiState
import com.aipose.camera.posematch.ui.screens.photoEdit.models.photoFilterStyle
import com.aipose.camera.posematch.util.bidiIsolate

private val CropImageInset = 20.dp
private val BadgeSideInset = 16.dp
private val BadgeTopInset = 48.dp
private val CompareBottomInset = 26.dp

@Composable
internal fun PhotoEditWorkspace(
    uiState: PhotoEditUiState,
    imagePath: String,
    onCompareChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCropping = uiState.selectedTab == PhotoEditTab.Crop
    val frameSize = if (isCropping) uiState.rotatedSourceSize else uiState.outputSize
    PhotoEditPreview(
        imagePath = imagePath,
        colorFilter = uiState.previewGrade?.let { grade ->
            ColorFilter.colorMatrix(ColorMatrix(grade.values.toFloatArray()))
        },
        geometry = uiState.geometry,
        frameAspect = frameSize.aspectOrNull(),
        imageInset = if (isCropping) CropImageInset else 0.dp,
        onCompareChange = if (isCropping) null else onCompareChange,
        modifier = modifier,
    ) {
        if (isCropping) {
            CropFrameOverlay(
                aspect = uiState.geometry.cropAspect,
                outputLabel = uiState.outputSize.outputLabel(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(CropImageInset),
            )
        } else {
            if (uiState.selectedTab == PhotoEditTab.Filters) {
                PhotoEditFrameOverlay(modifier = Modifier.fillMaxSize())
                EditLookChip(
                    label = stringResource(
                        R.string.edit_look_chip,
                        stringResource(photoFilterStyle(uiState.selectedFilter).labelRes),
                        stringResource(uiState.cropAspect.labelRes),
                    ),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = BadgeTopInset, end = BadgeSideInset),
                )
            }
            uiState.matchScore?.let { score ->
                EditMatchBadge(
                    score = score,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = BadgeTopInset, start = BadgeSideInset),
                )
            }
            if (uiState.selectedTab == PhotoEditTab.Adjust) {
                EditCompareChip(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = BadgeSideInset, bottom = CompareBottomInset),
                )
            }
        }
    }
}

private fun PhotoSize.aspectOrNull(): Float? =
    if (isValid) width.toFloat() / height else null

@Composable
private fun PhotoSize.outputLabel(): String =
    if (isValid) stringResource(R.string.edit_output_size, width, height).bidiIsolate() else ""
