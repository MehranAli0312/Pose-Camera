package com.aipose.camera.posematch.ui.screens.photoEdit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import com.aipose.camera.posematch.ui.common.safeBottomSystemBarsPadding
import androidx.compose.foundation.layout.padding
import com.aipose.camera.posematch.ui.common.safeTopSystemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.ui.common.PoseGlowBackground
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.common.PoseGlows
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.screens.photoEdit.components.AdjustPanel
import com.aipose.camera.posematch.ui.screens.photoEdit.components.CropPanel
import com.aipose.camera.posematch.ui.screens.photoEdit.components.FiltersPanel
import com.aipose.camera.posematch.ui.screens.photoEdit.components.PhotoEditTabRow
import com.aipose.camera.posematch.ui.screens.photoEdit.components.PhotoEditTopBar
import com.aipose.camera.posematch.ui.screens.photoEdit.components.PhotoEditWorkspace
import com.aipose.camera.posematch.ui.screens.photoEdit.models.PhotoEditTab
import com.aipose.camera.posematch.ui.vm.PhotoEditViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PhotoEditScreen(
    navController: NavHostController,
    viewModel: PhotoEditViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedCaptureId) {
        val captureId = uiState.savedCaptureId ?: return@LaunchedEffect
        viewModel.onSavedCaptureHandled()
        navController.navigate(NavRoute.PhotoSuccessScreenRoute.routeFor(captureId)) {
            popUpTo(NavRoute.PhotoEditScreenRoute.route) { inclusive = true }
        }
    }

    val draft = uiState.draft ?: return

    PoseGlowBackground(glows = PoseGlows.PhotoEdit) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveWidth()
                .safeTopSystemBarsPadding()
                .safeBottomSystemBarsPadding()
        ) {
            PhotoEditTopBar(
                isSaving = uiState.isSaving,
                onDiscard = {
                    viewModel.discard()
                    navController.popBackStack()
                },
                onReset = viewModel::reset,
                onSave = viewModel::save,
            )

            PhotoEditWorkspace(
                uiState = uiState,
                imagePath = draft.imagePath,
                onCompareChange = viewModel::setComparing,
                onCropRectChange = viewModel::setCropRect,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
            )

            PhotoEditTabRow(
                selectedTab = uiState.selectedTab,
                onTabSelected = viewModel::selectTab,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState.selectedTab) {
                PhotoEditTab.Filters -> FiltersPanel(
                    selectedFilter = uiState.selectedFilter,
                    intensity = uiState.filterIntensity,
                    onFilterSelected = viewModel::selectFilter,
                    onIntensityChange = viewModel::setFilterIntensity,
                )

                PhotoEditTab.Adjust -> AdjustPanel(
                    activeTool = uiState.activeTool,
                    value = uiState.activeToolValue,
                    isToolTouched = uiState::isToolTouched,
                    onToolSelected = viewModel::selectTool,
                    onValueChange = viewModel::setActiveToolValue,
                    onResetAdjustments = viewModel::resetAdjustments,
                )

                PhotoEditTab.Crop -> CropPanel(
                    selectedAspect = uiState.cropAspect,
                    straightenDegrees = uiState.geometry.straightenDegrees,
                    onAspectSelected = { aspect -> viewModel.setCropAspect(aspect.ratio) },
                    onTransform = viewModel::applyTransform,
                    onStraightenChange = viewModel::setStraighten,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
