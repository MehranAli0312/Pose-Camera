package com.aipose.camera.posematch.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.ui.common.POSE_ROW_SIZE
import com.aipose.camera.posematch.ui.common.PoseThumbRow
import com.aipose.camera.posematch.ui.common.StudioSearchField
import com.example.common.showToast
import com.aipose.camera.posematch.ui.common.rememberPosePicker
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.screens.home.components.HomeTapHint
import com.aipose.camera.posematch.ui.screens.home.components.HomeTopBar
import com.aipose.camera.posematch.ui.screens.home.components.PoseCategoryHeader
import com.aipose.camera.posematch.ui.screens.home.components.PoseHeroCard
import com.aipose.camera.posematch.ui.screens.home.components.PoseSearchEmptyCard
import com.aipose.camera.posematch.ui.screens.home.models.HomeUiState
import com.aipose.camera.posematch.ui.vm.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val importedPose by viewModel.importedPose.collectAsStateWithLifecycle()
    val importFailed by viewModel.importFailed.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val importedMessage = stringResource(R.string.toast_reference_imported)
    val importFailedMessage = stringResource(R.string.toast_reference_failed)

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val importTitle = stringResource(R.string.imported_pose_title)

    val posePicker = rememberPosePicker { pickedUri ->
        viewModel.importPose(importTitle, pickedUri)
    }

    fun dismissKeyboard() {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    fun openCamera(pose: Pose) {
        dismissKeyboard()
        viewModel.clearSearch()
        navController.navigateOnClick(NavRoute.CameraScreenRoute.routeFor(pose.id))
    }

    LaunchedEffect(importedPose) {
        importedPose?.let { pose ->
            viewModel.consumeImportedPose()
            context.showToast(importedMessage)
            openCamera(pose)
        }
    }

    LaunchedEffect(importFailed) {
        if (importFailed) {
            viewModel.consumeImportFailure()
            context.showToast(importFailedMessage)
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearSearch() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 20.dp),
    ) {
        item(key = HOME_TOP_BAR_KEY) {
            HomeTopBar(
                onImportPose = {
                    dismissKeyboard()
                    posePicker()
                },
                onOpenPro = {
                    dismissKeyboard()
                    navController.navigateOnClick(NavRoute.ProScreenRoute.route)
                },
            )
        }

        item(key = HOME_SEARCH_KEY) {
            StudioSearchField(
                query = query,
                hint = stringResource(R.string.home_search_hint),
                onQueryChange = viewModel::setSearchQuery,
                onSearchSubmitted = { dismissKeyboard() },
            )
        }

        val content = uiState as? HomeUiState.Content ?: return@LazyColumn
        val searchResults = content.searchResults
        if (searchResults != null) {
            if (searchResults.isEmpty()) {
                item(key = HOME_SEARCH_EMPTY_KEY) { PoseSearchEmptyCard() }
            } else {
                searchResults.chunked(POSE_ROW_SIZE).forEachIndexed { index, rowPoses ->
                    item(key = HOME_SEARCH_ROW_KEY + index) {
                        PoseThumbRow(poses = rowPoses, onPoseClick = ::openCamera)
                    }
                }
            }
            return@LazyColumn
        }

        item(key = HOME_HERO_KEY) {
            PoseHeroCard(
                heroImagePath = content.heroPose?.imagePath,
                onStartPosing = { content.heroPose?.let(::openCamera) },
            )
        }

        item(key = HOME_HINT_KEY) { HomeTapHint() }

        content.sections.forEach { section ->
            item(key = HOME_CATEGORY_HEADER_KEY + section.category) {
                PoseCategoryHeader(
                    category = section.category,
                    showAll = section.hasMore,
                    onShowAll = {
                        dismissKeyboard()
                        navController.navigateOnClick(
                            NavRoute.PoseAlbumScreenRoute.routeFor(section.category)
                        )
                    },
                )
            }
            item(key = HOME_CATEGORY_ROW_KEY + section.category) {
                PoseThumbRow(poses = section.previewPoses, onPoseClick = ::openCamera)
            }
        }
    }
}

private const val HOME_TOP_BAR_KEY = "home_top_bar"
private const val HOME_SEARCH_KEY = "home_search"
private const val HOME_SEARCH_EMPTY_KEY = "home_search_empty"
private const val HOME_SEARCH_ROW_KEY = "home_search_row_"
private const val HOME_HERO_KEY = "home_hero"
private const val HOME_HINT_KEY = "home_hint"
private const val HOME_CATEGORY_HEADER_KEY = "home_category_header_"
private const val HOME_CATEGORY_ROW_KEY = "home_category_row_"
