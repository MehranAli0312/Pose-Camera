package com.aipose.camera.posematch.ui.screens.poseAlbum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import com.aipose.camera.posematch.ui.common.safeBottomSystemBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ads.PremiumRewarded
import com.aipose.camera.posematch.ads.rememberScreenAds
import com.aipose.camera.posematch.ui.common.PoseScreenGutter
import com.aipose.camera.posematch.ui.common.poseScreenPadding
import com.aipose.camera.posematch.ui.common.PoseGlowBackground
import com.aipose.camera.posematch.ui.common.PoseGlows
import com.aipose.camera.posematch.ui.common.StudioSearchField
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.graph.popBackStackOnClick
import com.aipose.camera.posematch.ui.screens.bottomSheet.PremiumFeatureBottomSheet
import com.aipose.camera.posematch.ui.screens.poseAlbum.components.ExploreCategoryChips
import com.aipose.camera.posematch.ui.screens.poseAlbum.components.ExplorePoseCard
import com.aipose.camera.posematch.ui.screens.poseAlbum.components.ExplorePosePlaceholderCard
import com.aipose.camera.posematch.ui.screens.poseAlbum.components.ExploreHeader
import com.aipose.camera.posematch.ui.screens.poseAlbum.components.ExploreSortSheet
import com.aipose.camera.posematch.ui.screens.poseAlbum.models.PoseAlbumUiState
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import com.aipose.camera.posematch.ui.vm.PoseAlbumViewModel
import com.example.ads.AdPlacement
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private val CardMinWidth = 150.dp
private val GridGap = 20.dp

@Composable
fun PoseAlbumScreen(
    navController: NavHostController,
    category: String,
    viewModel: PoseAlbumViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val unlockedPoseToOpen by viewModel.unlockedPoseToOpen.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val screenAds = rememberScreenAds()
    val scope = rememberCoroutineScope()

    LaunchedEffect(category) { viewModel.onCategoryRequested(category) }

    LaunchedEffect(unlockedPoseToOpen) {
        val poseId = unlockedPoseToOpen ?: return@LaunchedEffect
        viewModel.consumeUnlockedPose()
        navController.navigateOnClick(NavRoute.PoseDetailScreenRoute.routeFor(poseId))
    }

    val content = uiState as? PoseAlbumUiState.Content
    val hasLockedPoses = content?.lockedPoseIds?.isNotEmpty() == true

    LaunchedEffect(hasLockedPoses) {
        if (hasLockedPoses) screenAds.preload(AdPlacement.PremiumRewarded)
    }

    PoseGlowBackground(glows = PoseGlows.Collections) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveWidth()
                .safeBottomSystemBarsPadding(),
        ) {
            ExploreHeader(
                onBack = navController::popBackStackOnClick,
                onOpenSort = viewModel::showSortSheet,
                modifier = Modifier.poseScreenPadding(),
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(CardMinWidth),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = PoseScreenGutter,
                    end = PoseScreenGutter,
                    top = GridGap,
                    bottom = 24.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(GridGap),
                verticalArrangement = Arrangement.spacedBy(GridGap),
            ) {
                item(key = SEARCH_KEY, span = { GridItemSpan(maxLineSpan) }) {
                    StudioSearchField(
                        query = query,
                        hint = stringResource(R.string.explore_search_hint),
                        onQueryChange = viewModel::setQuery,
                        onSearchSubmitted = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                if (content == null) {
                    items(PLACEHOLDER_CARD_COUNT, key = { index -> PLACEHOLDER_KEY + index }) {
                        ExplorePosePlaceholderCard()
                    }
                } else {
                    item(key = CHIPS_KEY, span = { GridItemSpan(maxLineSpan) }) {
                        ExploreCategoryChips(
                            categories = content.categories,
                            selectedCategory = content.selectedCategory,
                            onSelect = viewModel::selectCategory,
                        )
                    }
                    if (content.poses.isEmpty()) {
                        item(key = EMPTY_KEY, span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = stringResource(R.string.explore_empty),
                                style = poseTextStyle(12.5.sp, FontWeight.Normal, LocalAppPalette.current.textMuted),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                            )
                        }
                    }
                    items(content.poses, key = { pose -> pose.id }) { pose ->
                        val isLocked = pose.id in content.lockedPoseIds
                        ExplorePoseCard(
                            pose = pose,
                            isSaved = pose.id in content.savedPoseIds,
                            isLocked = isLocked,
                            onClick = {
                                if (isLocked) {
                                    viewModel.showLockedPose(pose)
                                } else {
                                    navController.navigateOnClick(
                                        NavRoute.PoseDetailScreenRoute.routeFor(pose.id)
                                    )
                                }
                            },
                            onToggleSaved = { viewModel.toggleSaved(pose) },
                        )
                    }
                }
            }
        }
    }

    content?.lockedPose?.let { lockedPose ->
        PremiumFeatureBottomSheet(
            posePreviewPath = lockedPose.imagePath,
            poseTitle = lockedPose.title,
            isAdLoading = content.isUnlockAdLoading,
            onWatchAdClick = {
                viewModel.onUnlockAdStarted()
                scope.launch {
                    val result = screenAds.rewarded(
                        placement = AdPlacement.PremiumRewarded,
                        onShown = viewModel::onUnlockAdShown,
                    )
                    viewModel.onUnlockAdFinished(lockedPose, result.wasRewarded)
                }
            },
            onGoPremiumClick = {
                viewModel.dismissLockedPose()
                navController.navigateOnClick(NavRoute.ProScreenRoute.route)
            },
            onDismissRequest = viewModel::dismissLockedPose,
        )
    }

    if (content?.isSortSheetVisible == true) {
        ExploreSortSheet(
            selected = content.sort,
            onSelect = viewModel::selectSort,
            onDismiss = viewModel::dismissSortSheet,
        )
    }
}

private const val SEARCH_KEY = "explore_search"
private const val CHIPS_KEY = "explore_chips"
private const val EMPTY_KEY = "explore_empty"
private const val PLACEHOLDER_KEY = "explore_placeholder_"
private const val PLACEHOLDER_CARD_COUNT = 6
