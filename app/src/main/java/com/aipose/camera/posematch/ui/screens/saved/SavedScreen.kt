package com.aipose.camera.posematch.ui.screens.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.ads.PremiumRewarded
import com.aipose.camera.posematch.ads.rememberScreenAds
import com.aipose.camera.posematch.ui.common.PoseGlowBackground
import com.aipose.camera.posematch.ui.common.PoseGlows
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.common.poseScreenPadding
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.screens.bottomSheet.PremiumFeatureBottomSheet
import com.aipose.camera.posematch.ui.screens.saved.components.SavedEmptyState
import com.aipose.camera.posematch.ui.screens.saved.components.SavedHeader
import com.aipose.camera.posematch.ui.screens.saved.components.SavedPoseRow
import com.aipose.camera.posematch.ui.screens.saved.components.SavedShotCard
import com.aipose.camera.posematch.ui.screens.saved.components.SavedSortSheet
import com.aipose.camera.posematch.ui.screens.saved.components.SavedTabs
import com.aipose.camera.posematch.ui.screens.saved.models.SavedPose
import com.aipose.camera.posematch.ui.screens.saved.models.SavedShot
import com.aipose.camera.posematch.ui.screens.saved.models.SavedTab
import com.aipose.camera.posematch.ui.screens.saved.models.SavedUiState
import com.aipose.camera.posematch.ui.vm.SavedViewModel
import com.example.ads.AdPlacement
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private const val SHOTS_PER_ROW = 2

@Composable
fun SavedScreen(
    navController: NavHostController,
    viewModel: SavedViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSortSheetVisible by viewModel.isSortSheetVisible.collectAsStateWithLifecycle()
    val unlockPrompt by viewModel.unlockPrompt.collectAsStateWithLifecycle()
    val unlockedPoseToOpen by viewModel.unlockedPoseToOpen.collectAsStateWithLifecycle()
    val content = uiState as? SavedUiState.Content
    val screenAds = rememberScreenAds()
    val scope = rememberCoroutineScope()
    val hasLockedPoses = content?.poses?.any { it.isLocked } == true

    LaunchedEffect(hasLockedPoses) {
        if (hasLockedPoses) screenAds.preload(AdPlacement.PremiumRewarded)
    }

    LaunchedEffect(unlockedPoseToOpen) {
        val poseId = unlockedPoseToOpen ?: return@LaunchedEffect
        viewModel.consumeUnlockedPose()
        navController.navigateOnClick(NavRoute.PoseDetailScreenRoute.routeFor(poseId))
    }

    fun openShot(shot: SavedShot) {
        navController.navigateOnClick(
            NavRoute.CaptureDetailScreenRoute.routeFor(shot.capture.id)
        )
    }

    fun openPose(saved: SavedPose) {
        if (saved.isLocked) {
            viewModel.showLockedPose(saved)
            return
        }
        navController.navigateOnClick(
            NavRoute.PoseDetailScreenRoute.routeFor(saved.pose.id)
        )
    }

    PoseGlowBackground(glows = PoseGlows.Saved) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveWidth()
                .poseScreenPadding(),
        ) {
            SavedHeader(
                totalCount = content?.totalCount ?: 0,
                onOpenSort = viewModel::showSortSheet,
            )
            Spacer(modifier = Modifier.height(19.dp))

            if (content == null) return@PoseGlowBackground

            SavedTabs(
                selected = content.selectedTab,
                shotsCount = content.shots.size,
                posesCount = content.poses.size,
                onSelect = viewModel::selectTab,
            )
            Spacer(modifier = Modifier.height(17.dp))

            val isTabEmpty = when (content.selectedTab) {
                SavedTab.Shots -> content.shots.isEmpty()
                SavedTab.Poses -> content.poses.isEmpty()
            }

            if (isTabEmpty) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    SavedEmptyState(tab = content.selectedTab)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    when (content.selectedTab) {
                        SavedTab.Shots ->
                            shotRows(content.shots, ::openShot, viewModel::unsaveShot)

                        SavedTab.Poses ->
                            poseRows(content.poses, ::openPose, viewModel::unsavePose)
                    }
                }
            }
        }
    }

    unlockPrompt?.let { prompt ->
        PremiumFeatureBottomSheet(
            posePreviewPath = prompt.pose.imagePath,
            poseTitle = prompt.pose.title,
            isAdLoading = prompt.isAdLoading,
            onWatchAdClick = {
                viewModel.onUnlockAdStarted()
                scope.launch {
                    val result = screenAds.rewarded(
                        placement = AdPlacement.PremiumRewarded,
                        onShown = viewModel::onUnlockAdShown,
                    )
                    viewModel.onUnlockAdFinished(prompt.pose, result.wasRewarded)
                }
            },
            onGoPremiumClick = {
                viewModel.dismissLockedPose()
                navController.navigateOnClick(NavRoute.ProScreenRoute.route)
            },
            onDismissRequest = viewModel::dismissLockedPose,
        )
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

private fun LazyListScope.shotRows(
    shots: List<SavedShot>,
    onOpenShot: (SavedShot) -> Unit,
    onUnsaveShot: (SavedShot) -> Unit,
) {
    shots.chunked(SHOTS_PER_ROW).forEachIndexed { index, rowShots ->
        item(key = SHOTS_ROW_KEY + index) {
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

private const val SHOTS_ROW_KEY = "saved_shots_row_"
private const val POSES_ROW_KEY = "saved_poses_row_"
