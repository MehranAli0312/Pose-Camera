package com.aipose.camera.posematch.ui.screens.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.ui.common.PoseGlowBackground
import com.aipose.camera.posematch.ui.common.PoseGlows
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.screens.saved.components.SavedEmptyState
import com.aipose.camera.posematch.ui.screens.saved.components.SavedHeader
import com.aipose.camera.posematch.ui.screens.saved.components.SavedPoseRow
import com.aipose.camera.posematch.ui.screens.saved.components.SavedSectionHeader
import com.aipose.camera.posematch.ui.screens.saved.components.SavedShotCard
import com.aipose.camera.posematch.ui.screens.saved.components.SavedSortSheet
import com.aipose.camera.posematch.ui.screens.saved.components.SavedTabs
import com.aipose.camera.posematch.ui.screens.saved.models.SavedPose
import com.aipose.camera.posematch.ui.screens.saved.models.SavedShot
import com.aipose.camera.posematch.ui.screens.saved.models.SavedTab
import com.aipose.camera.posematch.ui.screens.saved.models.SavedUiState
import com.aipose.camera.posematch.ui.vm.SavedViewModel
import org.koin.androidx.compose.koinViewModel

private const val SHOTS_PER_ROW = 2
private const val POSE_PREVIEW_SIZE = 2
private const val SHOT_PREVIEW_ROWS = 1

@Composable
fun SavedScreen(
    navController: NavHostController,
    viewModel: SavedViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSortSheetVisible by viewModel.isSortSheetVisible.collectAsStateWithLifecycle()
    val content = uiState as? SavedUiState.Content

    fun openShot(shot: SavedShot) {
        navController.navigateOnClick(
            NavRoute.CaptureDetailScreenRoute.routeFor(shot.capture.id)
        )
    }

    fun openPose(saved: SavedPose) {
        navController.navigateOnClick(
            NavRoute.PoseDetailScreenRoute.routeFor(saved.pose.id)
        )
    }

    PoseGlowBackground(glows = PoseGlows.Saved) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item(key = HEADER_KEY) {
                Spacer(modifier = Modifier.height(14.dp))
                SavedHeader(
                    totalCount = content?.totalCount ?: 0,
                    onOpenSort = viewModel::showSortSheet,
                )
                Spacer(modifier = Modifier.height(19.dp))
            }

            if (content == null) return@LazyColumn

            item(key = TABS_KEY) {
                SavedTabs(
                    selected = content.selectedTab,
                    shotsCount = content.shots.size,
                    posesCount = content.poses.size,
                    onSelect = viewModel::selectTab,
                )
                Spacer(modifier = Modifier.height(23.dp))
            }

            when (content.selectedTab) {
                SavedTab.Shots -> shotsTab(
                    content = content,
                    onOpenShot = ::openShot,
                    onUnsaveShot = viewModel::unsaveShot,
                    onOpenPose = ::openPose,
                    onUnsavePose = viewModel::unsavePose,
                    onSeeAllPoses = { viewModel.selectTab(SavedTab.Poses) },
                )

                SavedTab.Poses -> posesTab(
                    content = content,
                    onOpenPose = ::openPose,
                    onUnsavePose = viewModel::unsavePose,
                    onOpenShot = ::openShot,
                    onUnsaveShot = viewModel::unsaveShot,
                    onSeeAllShots = { viewModel.selectTab(SavedTab.Shots) },
                )
            }
        }
    }

    if (isSortSheetVisible && content != null) {
        SavedSortSheet(
            selected = content.sort,
            tab = content.selectedTab,
            onSelect = viewModel::selectSort,
            onDismiss = viewModel::dismissSortSheet,
        )
    }
}

private fun LazyListScope.shotsTab(
    content: SavedUiState.Content,
    onOpenShot: (SavedShot) -> Unit,
    onUnsaveShot: (SavedShot) -> Unit,
    onOpenPose: (SavedPose) -> Unit,
    onUnsavePose: (SavedPose) -> Unit,
    onSeeAllPoses: () -> Unit,
) {
    item(key = SHOTS_LABEL_KEY) {
        SavedSectionHeader(titleRes = SavedTab.Shots.sectionRes)
        Spacer(modifier = Modifier.height(8.dp))
    }
    if (content.shots.isEmpty()) {
        item(key = SHOTS_EMPTY_KEY) { SavedEmptyState(tab = SavedTab.Shots) }
    } else {
        shotRows(content.shots, onOpenShot, onUnsaveShot, SHOTS_ROW_KEY)
    }
    if (content.poses.isNotEmpty()) {
        item(key = POSES_LABEL_KEY) {
            Spacer(modifier = Modifier.height(29.dp))
            SavedSectionHeader(
                titleRes = SavedTab.Poses.sectionRes,
                onSeeAll = onSeeAllPoses.takeIf { content.poses.size > POSE_PREVIEW_SIZE },
            )
            Spacer(modifier = Modifier.height(23.dp))
        }
        poseRows(content.poses.take(POSE_PREVIEW_SIZE), onOpenPose, onUnsavePose)
    }
}

private fun LazyListScope.posesTab(
    content: SavedUiState.Content,
    onOpenPose: (SavedPose) -> Unit,
    onUnsavePose: (SavedPose) -> Unit,
    onOpenShot: (SavedShot) -> Unit,
    onUnsaveShot: (SavedShot) -> Unit,
    onSeeAllShots: () -> Unit,
) {
    item(key = POSES_LABEL_KEY) {
        SavedSectionHeader(titleRes = SavedTab.Poses.sectionRes)
        Spacer(modifier = Modifier.height(23.dp))
    }
    if (content.poses.isEmpty()) {
        item(key = POSES_EMPTY_KEY) { SavedEmptyState(tab = SavedTab.Poses) }
    } else {
        poseRows(content.poses, onOpenPose, onUnsavePose)
    }
    if (content.shots.isNotEmpty()) {
        val preview = content.shots.take(SHOT_PREVIEW_ROWS * SHOTS_PER_ROW)
        item(key = SHOTS_LABEL_KEY) {
            Spacer(modifier = Modifier.height(29.dp))
            SavedSectionHeader(
                titleRes = SavedTab.Shots.sectionRes,
                onSeeAll = onSeeAllShots.takeIf { content.shots.size > preview.size },
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        shotRows(preview, onOpenShot, onUnsaveShot, SHOTS_PREVIEW_ROW_KEY)
    }
}

private fun LazyListScope.shotRows(
    shots: List<SavedShot>,
    onOpenShot: (SavedShot) -> Unit,
    onUnsaveShot: (SavedShot) -> Unit,
    keyPrefix: String,
) {
    shots.chunked(SHOTS_PER_ROW).forEachIndexed { index, rowShots ->
        item(key = keyPrefix + index) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                rowShots.forEach { shot ->
                    SavedShotCard(
                        shot = shot,
                        onClick = { onOpenShot(shot) },
                        onUnsave = { onUnsaveShot(shot) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(SHOTS_PER_ROW - rowShots.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun LazyListScope.poseRows(
    poses: List<SavedPose>,
    onOpenPose: (SavedPose) -> Unit,
    onUnsavePose: (SavedPose) -> Unit,
) {
    poses.forEach { saved ->
        item(key = POSES_ROW_KEY + saved.pose.id) {
            SavedPoseRow(
                saved = saved,
                onClick = { onOpenPose(saved) },
                onUnsave = { onUnsavePose(saved) },
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
    }
}

private const val HEADER_KEY = "saved_header"
private const val TABS_KEY = "saved_tabs"
private const val SHOTS_LABEL_KEY = "saved_shots_label"
private const val SHOTS_ROW_KEY = "saved_shots_row_"
private const val SHOTS_PREVIEW_ROW_KEY = "saved_shots_preview_row_"
private const val SHOTS_EMPTY_KEY = "saved_shots_empty"
private const val POSES_LABEL_KEY = "saved_poses_label"
private const val POSES_ROW_KEY = "saved_poses_row_"
private const val POSES_EMPTY_KEY = "saved_poses_empty"
