package com.aipose.camera.posematch.ui.screens.collections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.AnalyticsEvent
import com.aipose.camera.posematch.ui.common.PoseGlowBackground
import com.aipose.camera.posematch.ui.common.PoseGlows
import com.aipose.camera.posematch.ui.common.StudioSearchField
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.common.poseScreenPadding
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.graph.popBackStackOnClick
import com.aipose.camera.posematch.ui.screens.bottomSheet.PremiumFeatureBottomSheet
import com.aipose.camera.posematch.ui.screens.collections.components.CaptureAlbumSection
import com.aipose.camera.posematch.ui.screens.collections.components.CollectionsEmptyState
import com.aipose.camera.posematch.ui.screens.collections.components.CollectionsFilterChips
import com.aipose.camera.posematch.ui.screens.collections.components.CollectionsHeader
import com.aipose.camera.posematch.ui.screens.collections.components.FavoritePosesEmptyState
import com.aipose.camera.posematch.ui.screens.collections.components.PerfectShotsCard
import com.aipose.camera.posematch.ui.screens.collections.components.favoritePoseGridItems
import com.aipose.camera.posematch.ui.screens.collections.models.AlbumAccent
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsFilter
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsUiState
import com.aipose.camera.posematch.ui.screens.poseAlbum.models.adaptiveColumnCount
import com.aipose.camera.posematch.ui.vm.CollectionsViewModel
import org.koin.androidx.compose.koinViewModel

private val EmptyStateHeight = 320.dp
private val FavoriteCardMinWidth = 150.dp
private val FavoriteGridSidePadding = 15.dp
private val FavoriteCardGap = 10.dp

@Composable
fun CollectionsScreen(
    navController: NavHostController,
    viewModel: CollectionsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val lockedPose by viewModel.lockedPose.collectAsStateWithLifecycle()
    val unlockedPoseToOpen by viewModel.unlockedPoseToOpen.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) { viewModel.trackEvent(AnalyticsEvent.Collections) }

    LaunchedEffect(unlockedPoseToOpen) {
        val poseId = unlockedPoseToOpen ?: return@LaunchedEffect
        viewModel.consumeUnlockedPose()
        navController.navigateOnClick(NavRoute.CameraScreenRoute.routeFor(poseId))
    }

    fun dismissKeyboard() {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearQuery() }
    }

    val content = uiState as? CollectionsUiState.Content

    PoseGlowBackground(glows = PoseGlows.Collections) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveWidth(),
        ) {
            CollectionsHeader(
                onBack = navController::popBackStackOnClick,
                modifier = Modifier.poseScreenPadding(),
            )
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val favoriteColumns = adaptiveColumnCount(
                    availableWidth = maxWidth - FavoriteGridSidePadding * 2,
                    minCellWidth = FavoriteCardMinWidth,
                    gap = FavoriteCardGap,
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item(key = SEARCH_KEY) {
                        StudioSearchField(
                            query = query,
                            hint = stringResource(R.string.collections_search_hint),
                            onQueryChange = viewModel::setQuery,
                            onSearchSubmitted = { dismissKeyboard() },
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                    if (content != null) {
                        item(key = FILTERS_KEY) {
                            CollectionsFilterChips(
                                selected = content.filter,
                                onSelect = viewModel::selectFilter,
                            )
                        }
                        if (content.filter == CollectionsFilter.Favorite) {
                            if (content.favoritePoses.isEmpty()) {
                                item(key = EMPTY_KEY) {
                                    if (query.isBlank()) {
                                        FavoritePosesEmptyState(
                                            modifier = Modifier.height(EmptyStateHeight),
                                        )
                                    } else {
                                        CollectionsEmptyState(
                                            query = query,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(EmptyStateHeight),
                                        )
                                    }
                                }
                            }
                            favoritePoseGridItems(
                                favorites = content.favoritePoses,
                                columns = favoriteColumns,
                                sidePadding = FavoriteGridSidePadding,
                                cardGap = FavoriteCardGap,
                                onPoseClick = { favorite ->
                                    if (favorite.isLocked) {
                                        viewModel.showLockedPose(favorite)
                                    } else {
                                        navController.navigateOnClick(
                                            NavRoute.CameraScreenRoute.routeFor(favorite.pose.id)
                                        )
                                    }
                                },
                                onRemove = viewModel::removeFavoritePose,
                            )
                        } else {
                            if (content.albums.isEmpty()) {
                                item(key = EMPTY_KEY) {
                                    CollectionsEmptyState(
                                        query = query,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(EmptyStateHeight),
                                    )
                                }
                            }
                            items(
                                content.albums,
                                key = { album -> ALBUM_KEY + album.locationLabel },
                            ) { album ->
                                CaptureAlbumSection(
                                    album = album,
                                    accent = AlbumAccent.forLabel(album.locationLabel),
                                    onCaptureClick = { captureUi ->
                                        dismissKeyboard()
                                        navController.navigateOnClick(
                                            NavRoute.CaptureDetailScreenRoute.routeFor(captureUi.id)
                                        )
                                    },
                                )
                            }
                            if (content.recentPerfectShots > 0) {
                                item(key = PERFECT_KEY) {
                                    PerfectShotsCard(
                                        count = content.recentPerfectShots,
                                        onClick = { viewModel.selectFilter(CollectionsFilter.TopMatch) },
                                        modifier = Modifier.padding(horizontal = 20.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    lockedPose?.let { pose ->
        PremiumFeatureBottomSheet(
            pose = pose,
            onRewardEarned = { viewModel.unlockAfterRewardedAd(pose) },
            onGoPremiumClick = {
                viewModel.dismissLockedPose()
                navController.navigateOnClick(NavRoute.ProScreenRoute.route)
            },
            onDismissRequest = viewModel::dismissLockedPose,
        )
    }
}

private const val SEARCH_KEY = "collections_search"
private const val FILTERS_KEY = "collections_filters"
private const val EMPTY_KEY = "collections_empty"
private const val PERFECT_KEY = "collections_perfect"
private const val ALBUM_KEY = "collections_album_"
