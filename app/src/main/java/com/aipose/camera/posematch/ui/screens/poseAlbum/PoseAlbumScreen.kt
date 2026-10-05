package com.aipose.camera.posematch.ui.screens.poseAlbum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import com.aipose.camera.posematch.ui.common.safeBottomSystemBarsPadding
import androidx.compose.foundation.layout.padding
import com.aipose.camera.posematch.ui.common.safeTopSystemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.aipose.camera.posematch.ui.common.PoseScreenGutter
import com.aipose.camera.posematch.ui.common.PoseScreenTopSpacing
import com.aipose.camera.posematch.ui.common.PoseGlowBackground
import com.aipose.camera.posematch.ui.common.PoseGlows
import com.aipose.camera.posematch.ui.common.StudioSearchField
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.graph.popBackStackOnClick
import com.aipose.camera.posematch.ui.screens.poseAlbum.components.ExploreCategoryChips
import com.aipose.camera.posematch.ui.screens.poseAlbum.components.ExploreHeader
import com.aipose.camera.posematch.ui.screens.poseAlbum.components.ExplorePoseCard
import com.aipose.camera.posematch.ui.screens.poseAlbum.components.ExploreResultsHeader
import com.aipose.camera.posematch.ui.screens.poseAlbum.components.ExploreSortSheet
import com.aipose.camera.posematch.ui.screens.poseAlbum.models.PoseAlbumUiState
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import com.aipose.camera.posematch.ui.vm.PoseAlbumViewModel
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
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(category) { viewModel.onCategoryRequested(category) }

    val content = uiState as? PoseAlbumUiState.Content ?: return

    PoseGlowBackground(glows = PoseGlows.Collections) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(CardMinWidth),
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveWidth()
                .safeTopSystemBarsPadding()
                .safeBottomSystemBarsPadding(),
            contentPadding = PaddingValues(
                start = PoseScreenGutter,
                end = PoseScreenGutter,
                top = PoseScreenTopSpacing,
                bottom = 24.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(GridGap),
            verticalArrangement = Arrangement.spacedBy(GridGap),
        ) {
            item(key = HEADER_KEY, span = { GridItemSpan(maxLineSpan) }) {
                ExploreHeader(
                    totalCount = content.totalCount,
                    onBack = navController::popBackStackOnClick,
                )
            }
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
            item(key = CHIPS_KEY, span = { GridItemSpan(maxLineSpan) }) {
                ExploreCategoryChips(
                    totalCount = content.totalCount,
                    categories = content.categories,
                    selectedCategory = content.selectedCategory,
                    onSelect = viewModel::selectCategory,
                )
            }
            item(key = RESULTS_KEY, span = { GridItemSpan(maxLineSpan) }) {
                ExploreResultsHeader(
                    count = content.poses.size,
                    onOpenSort = viewModel::showSortSheet,
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
                ExplorePoseCard(
                    pose = pose,
                    isSaved = pose.id in content.savedPoseIds,
                    onClick = {
                        navController.navigateOnClick(NavRoute.PoseDetailScreenRoute.routeFor(pose.id))
                    },
                    onToggleSaved = { viewModel.toggleSaved(pose) },
                )
            }
        }
    }

    if (content.isSortSheetVisible) {
        ExploreSortSheet(
            selected = content.sort,
            onSelect = viewModel::selectSort,
            onDismiss = viewModel::dismissSortSheet,
        )
    }
}

private const val HEADER_KEY = "explore_header"
private const val SEARCH_KEY = "explore_search"
private const val CHIPS_KEY = "explore_chips"
private const val RESULTS_KEY = "explore_results"
private const val EMPTY_KEY = "explore_empty"
