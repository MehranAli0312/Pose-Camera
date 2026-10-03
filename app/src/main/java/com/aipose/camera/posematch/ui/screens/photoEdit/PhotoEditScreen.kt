package com.aipose.camera.posematch.ui.screens.photoEdit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.screens.photoEdit.components.AdjustSliderRow
import com.aipose.camera.posematch.ui.screens.photoEdit.components.AdjustToolRail
import com.aipose.camera.posematch.ui.screens.photoEdit.components.CropAspectChips
import com.aipose.camera.posematch.ui.screens.photoEdit.components.PhotoEditPreview
import com.aipose.camera.posematch.ui.screens.photoEdit.components.PhotoEditTabRow
import com.aipose.camera.posematch.ui.screens.photoEdit.components.PhotoEditTopBar
import com.aipose.camera.posematch.ui.screens.photoEdit.components.PhotoFilterStrip
import com.aipose.camera.posematch.ui.screens.photoEdit.components.RotateControls
import com.aipose.camera.posematch.ui.screens.photoEdit.models.AdjustTool
import com.aipose.camera.posematch.ui.screens.photoEdit.models.PhotoEditTab
import com.aipose.camera.posematch.ui.screens.photoEdit.models.valueOf
import com.aipose.camera.posematch.ui.screens.photoEdit.models.withValue
import com.aipose.camera.posematch.ui.theme.ReviewBackground
import com.aipose.camera.posematch.ui.vm.PhotoEditViewModel
import com.example.common.showToast
import org.koin.androidx.compose.koinViewModel

private const val ROTATE_LEFT_DEGREES = 3
private const val ROTATE_RIGHT_DEGREES = 1

@Composable
fun PhotoEditScreen(
    navController: NavHostController,
    viewModel: PhotoEditViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var activeTool by remember { mutableStateOf(AdjustTool.Exposure) }
    val context = LocalContext.current
    val savedTemplate = stringResource(R.string.toast_saved_location)
    val unknownLocation = stringResource(R.string.unknown_location)

    LaunchedEffect(uiState.savedPath) {
        val savedPath = uiState.savedPath ?: return@LaunchedEffect
        val locationName = uiState.savedLocationName ?: unknownLocation
        viewModel.onSavedPathHandled()
        context.showToast(savedTemplate.format(locationName))
        navController.navigateOnClick(NavRoute.PhotoSuccessScreenRoute.routeFor(savedPath))
    }

    val draft = uiState.draft ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReviewBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        PhotoEditTopBar(
            isSaving = uiState.isSaving,
            onDiscard = {
                viewModel.discard()
                navController.popBackStack()
            },
            onSave = viewModel::save,
        )

        PhotoEditPreview(
            imagePath = draft.imagePath,
            colorFilter = uiState.activeGrade?.let { grade ->
                ColorFilter.colorMatrix(ColorMatrix(grade.values.toFloatArray()))
            },
            rotationDegrees = uiState.rotationDegrees,
            cropAspect = uiState.cropAspect,
            modifier = Modifier.weight(1f),
        )

        PhotoEditTabRow(
            selectedTab = uiState.selectedTab,
            onTabSelected = viewModel::selectTab,
            onReset = viewModel::reset,
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            when (uiState.selectedTab) {
                PhotoEditTab.Filters -> PhotoFilterStrip(
                    imagePath = draft.imagePath,
                    selectedFilter = uiState.selectedFilter,
                    gradeFor = { filterId -> viewModel.previewGrade(filterId) },
                    onFilterSelected = viewModel::selectFilter,
                )

                PhotoEditTab.Adjust -> Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    AdjustToolRail(
                        activeTool = activeTool,
                        isToolTouched = { tool -> uiState.isToolTouched(tool) },
                        onToolSelected = { tool -> activeTool = tool },
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    when (activeTool) {
                        AdjustTool.Rotate -> RotateControls(
                            onRotateLeft = { repeat(ROTATE_LEFT_DEGREES) { viewModel.rotate() } },
                            onRotateRight = { repeat(ROTATE_RIGHT_DEGREES) { viewModel.rotate() } },
                        )

                        AdjustTool.Crop -> CropAspectChips(
                            selectedRatio = uiState.cropAspect,
                            onAspectSelected = viewModel::setCropAspect,
                        )

                        else -> AdjustSliderRow(
                            value = uiState.adjustments.valueOf(activeTool),
                            onValueChange = { value ->
                                viewModel.updateAdjustments(
                                    uiState.adjustments.withValue(activeTool, value)
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
