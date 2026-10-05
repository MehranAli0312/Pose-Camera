package com.aipose.camera.posematch.ui.screens.captureAlbum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseScreenGutter
import com.aipose.camera.posematch.ui.common.PoseScreenTopSpacing
import com.aipose.camera.posematch.ui.common.PoseDeleteDialog
import com.aipose.camera.posematch.ui.common.PoseGlowBackground
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.common.PoseGlows
import com.aipose.camera.posematch.ui.common.PoseStatsCard
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.graph.popBackStackOnClick
import com.aipose.camera.posematch.ui.models.PoseStat
import com.aipose.camera.posematch.ui.screens.captureAlbum.components.ALBUM_COLUMNS
import com.aipose.camera.posematch.ui.screens.captureAlbum.components.AlbumActions
import com.aipose.camera.posematch.ui.screens.captureAlbum.components.AlbumHeader
import com.aipose.camera.posematch.ui.screens.captureAlbum.components.AlbumShotRow
import com.aipose.camera.posematch.ui.screens.captureAlbum.models.CaptureAlbumUiState
import com.aipose.camera.posematch.ui.screens.collections.components.CollectionsFilterChips
import com.aipose.camera.posematch.ui.screens.collections.components.CollectionsSortSheet
import com.aipose.camera.posematch.ui.screens.collections.models.AlbumAccent
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsFilter
import com.aipose.camera.posematch.ui.theme.PoseCyanLight
import com.aipose.camera.posematch.ui.theme.PoseEmerald400
import com.aipose.camera.posematch.ui.vm.CaptureAlbumViewModel
import com.aipose.camera.posematch.util.bidiIsolate
import org.koin.androidx.compose.koinViewModel

private val AlbumFilters = listOf(
    CollectionsFilter.All,
    CollectionsFilter.TopMatch,
    CollectionsFilter.Recent,
)

@Composable
fun CaptureAlbumScreen(
    navController: NavHostController,
    locationLabel: String,
    viewModel: CaptureAlbumViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(locationLabel) { viewModel.onAlbumRequested(locationLabel) }

    LaunchedEffect(uiState) {
        if (uiState is CaptureAlbumUiState.Removed) navController.popBackStack()
    }

    val content = uiState as? CaptureAlbumUiState.Content ?: return
    val accent = AlbumAccent.forLabel(content.locationLabel)

    PoseGlowBackground(glows = PoseGlows.LocationAlbum) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveWidth()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(top = PoseScreenTopSpacing, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key = HEADER_KEY) {
                AlbumHeader(
                    locationLabel = content.locationLabel,
                    summary = albumSummary(content),
                    accent = accent,
                    onBack = navController::popBackStackOnClick,
                    onOpenSort = viewModel::showSortSheet,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item(key = FILTERS_KEY) {
                CollectionsFilterChips(
                    selected = content.filter,
                    onSelect = viewModel::selectFilter,
                    filters = AlbumFilters,
                    selectedPalette = accent.palette,
                    allCount = content.totalCount,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                )
            }
            itemsIndexed(
                items = content.shots.chunked(ALBUM_COLUMNS),
                key = { index, _ -> ROW_KEY + index },
            ) { _, rowShots ->
                AlbumShotRow(
                    shots = rowShots,
                    onShotClick = { shot ->
                        navController.navigateOnClick(NavRoute.CaptureDetailScreenRoute.routeFor(shot.id))
                    },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item(key = STATS_KEY) {
                PoseStatsCard(
                    title = stringResource(R.string.album_this_place),
                    stats = listOf(
                        PoseStat(
                            value = content.totalCount.toString(),
                            label = stringResource(R.string.collections_stat_shots),
                            valueColor = Color.White,
                        ),
                        PoseStat(
                            value = stringResource(R.string.score_percent, content.averageMatch).bidiIsolate(),
                            label = stringResource(R.string.album_stat_average),
                            valueColor = PoseCyanLight,
                        ),
                        PoseStat(
                            value = stringResource(R.string.score_percent, content.bestMatch).bidiIsolate(),
                            label = stringResource(R.string.album_stat_best),
                            valueColor = PoseEmerald400,
                        ),
                    ),
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp),
                )
            }
            item(key = ACTIONS_KEY) {
                Spacer(modifier = Modifier.height(20.dp))
                AlbumActions(
                    onShootAgain = {
                        navController.navigateOnClick(NavRoute.CameraScreenRoute.routeWithoutPose())
                    },
                    onRemoveAll = viewModel::showRemoveDialog,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
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

    if (content.isRemoveDialogVisible) {
        PoseDeleteDialog(
            title = stringResource(R.string.album_remove_title),
            message = pluralStringResource(
                R.plurals.album_remove_message,
                content.totalCount,
                content.totalCount,
                content.locationLabel,
            ),
            onConfirm = viewModel::removeAll,
            onDismiss = viewModel::dismissRemoveDialog,
        )
    }
}

@Composable
private fun albumSummary(content: CaptureAlbumUiState.Content): String {
    val lastShot = when (content.lastShotDaysAgo) {
        0 -> stringResource(R.string.album_last_today)
        1 -> stringResource(R.string.album_last_yesterday)
        else -> pluralStringResource(
            R.plurals.album_last_days_ago,
            content.lastShotDaysAgo,
            content.lastShotDaysAgo,
        )
    }
    return stringResource(
        R.string.album_summary,
        pluralStringResource(R.plurals.album_shot_count, content.totalCount, content.totalCount),
        stringResource(R.string.score_percent, content.averageMatch).bidiIsolate(),
        lastShot,
    )
}

private const val HEADER_KEY = "album_header"
private const val FILTERS_KEY = "album_filters"
private const val STATS_KEY = "album_stats"
private const val ACTIONS_KEY = "album_actions"
private const val ROW_KEY = "album_row_"
