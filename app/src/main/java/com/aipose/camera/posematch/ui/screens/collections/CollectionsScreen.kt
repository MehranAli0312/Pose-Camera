package com.aipose.camera.posematch.ui.screens.collections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.aipose.camera.posematch.ui.common.StudioSearchField
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.screens.collections.components.CaptureAlbumHeader
import com.aipose.camera.posematch.ui.screens.collections.components.CaptureGrid
import com.aipose.camera.posematch.ui.screens.collections.components.CollectionsEmptyState
import com.aipose.camera.posematch.ui.screens.collections.components.CollectionsHeader
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsUiState
import com.aipose.camera.posematch.ui.vm.CollectionsViewModel
import org.koin.androidx.compose.koinViewModel

private const val ALBUM_PREVIEW_SIZE = 6

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

    val content = uiState as? CollectionsUiState.Content

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        CollectionsHeader(totalCount = content?.totalCount ?: 0)
        Spacer(modifier = Modifier.height(12.dp))
        StudioSearchField(
            query = query,
            hint = stringResource(R.string.history_search_hint),
            onQueryChange = viewModel::setQuery,
            onSearchSubmitted = { dismissKeyboard() },
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (content == null || content.isEmpty) {
            CollectionsEmptyState(
                query = query,
                modifier = Modifier.weight(1f),
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            content.albums.forEach { album ->
                item(key = ALBUM_HEADER_KEY + album.locationLabel) {
                    CaptureAlbumHeader(
                        locationLabel = album.locationLabel,
                        count = album.count,
                        showAll = album.count > ALBUM_PREVIEW_SIZE,
                        onShowAll = {
                            dismissKeyboard()
                            navController.navigateOnClick(
                                NavRoute.CaptureAlbumScreenRoute.routeFor(album.locationLabel)
                            )
                        },
                    )
                }
                item(key = ALBUM_GRID_KEY + album.locationLabel) {
                    CaptureGrid(
                        captures = album.captures.take(ALBUM_PREVIEW_SIZE),
                        onCaptureClick = { captureUi ->
                            dismissKeyboard()
                            navController.navigateOnClick(
                                NavRoute.CaptureDetailScreenRoute.routeFor(captureUi.id)
                            )
                        },
                    )
                }
            }
        }
    }
}

private const val ALBUM_HEADER_KEY = "capture_album_header_"
private const val ALBUM_GRID_KEY = "capture_album_grid_"
