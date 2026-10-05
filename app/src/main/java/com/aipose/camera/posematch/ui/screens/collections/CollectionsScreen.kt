package com.aipose.camera.posematch.ui.screens.collections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import com.aipose.camera.posematch.ui.common.safeTopSystemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseScreenGutter
import com.aipose.camera.posematch.ui.common.PoseScreenTopSpacing
import com.aipose.camera.posematch.ui.common.PoseGlowBackground
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.common.PoseGlows
import com.aipose.camera.posematch.ui.common.StudioSearchField
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.screens.collections.components.CaptureAlbumSection
import com.aipose.camera.posematch.ui.screens.collections.components.CollectionsEmptyState
import com.aipose.camera.posematch.ui.screens.collections.components.CollectionsFilterChips
import com.aipose.camera.posematch.ui.screens.collections.components.CollectionsHeader
import com.aipose.camera.posematch.ui.screens.collections.components.CollectionsSortSheet
import com.aipose.camera.posematch.ui.screens.collections.components.CollectionsStatsCard
import com.aipose.camera.posematch.ui.screens.collections.components.PerfectShotsCard
import com.aipose.camera.posematch.ui.screens.collections.models.AlbumAccent
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsFilter
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsUiState
import com.aipose.camera.posematch.ui.vm.CollectionsViewModel
import org.koin.androidx.compose.koinViewModel

private val EmptyStateHeight = 320.dp

@Composable
fun CollectionsScreen(
    navController: NavHostController,
    viewModel: CollectionsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    fun dismissKeyboard() {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearQuery() }
    }

    val content = uiState as? CollectionsUiState.Content ?: return

    PoseGlowBackground(glows = PoseGlows.Collections) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveWidth()
                .safeTopSystemBarsPadding(),
            contentPadding = PaddingValues(top = PoseScreenTopSpacing, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = HEADER_KEY) {
                CollectionsHeader(
                    onOpenSort = viewModel::showSortSheet,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item(key = SEARCH_KEY) {
                StudioSearchField(
                    query = query,
                    hint = stringResource(R.string.collections_search_hint),
                    onQueryChange = viewModel::setQuery,
                    onSearchSubmitted = { dismissKeyboard() },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item(key = STATS_KEY) {
                CollectionsStatsCard(
                    stats = content.stats,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item(key = FILTERS_KEY) {
                CollectionsFilterChips(
                    selected = content.filter,
                    onSelect = viewModel::selectFilter,
                )
            }
            if (content.isEmpty) {
                item(key = EMPTY_KEY) {
                    CollectionsEmptyState(
                        query = query,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(EmptyStateHeight),
                    )
                }
            }
            items(content.albums, key = { album -> ALBUM_KEY + album.locationLabel }) { album ->
                CaptureAlbumSection(
                    album = album,
                    accent = AlbumAccent.forLabel(album.locationLabel),
                    onShowAll = {
                        dismissKeyboard()
                        navController.navigateOnClick(
                            NavRoute.CaptureAlbumScreenRoute.routeFor(album.locationLabel)
                        )
                    },
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

    if (content.isSortSheetVisible) {
        CollectionsSortSheet(
            selected = content.sort,
            onSelect = viewModel::selectSort,
            onDismiss = viewModel::dismissSortSheet,
        )
    }
}

private const val HEADER_KEY = "collections_header"
private const val SEARCH_KEY = "collections_search"
private const val STATS_KEY = "collections_stats"
private const val FILTERS_KEY = "collections_filters"
private const val EMPTY_KEY = "collections_empty"
private const val PERFECT_KEY = "collections_perfect"
private const val ALBUM_KEY = "collections_album_"
