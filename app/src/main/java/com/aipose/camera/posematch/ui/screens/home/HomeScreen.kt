package com.aipose.camera.posematch.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.ui.common.POSE_ROW_SIZE
import com.aipose.camera.posematch.ui.common.PoseGlowBackground
import com.aipose.camera.posematch.ui.common.PoseThumbRow
import com.aipose.camera.posematch.ui.common.StudioSearchField
import com.aipose.camera.posematch.ui.common.rememberPosePicker
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.screens.home.components.HomeFilterBar
import com.aipose.camera.posematch.ui.screens.home.components.HomeHeader
import com.aipose.camera.posematch.ui.screens.home.components.HomeHeroCard
import com.aipose.camera.posematch.ui.screens.home.components.HomePoseOfTheDayCard
import com.aipose.camera.posematch.ui.screens.home.components.HomeProgressCard
import com.aipose.camera.posematch.ui.screens.home.components.HomeQuickActionGrid
import com.aipose.camera.posematch.ui.screens.home.components.PoseSearchEmptyCard
import com.aipose.camera.posematch.ui.screens.home.models.HomeQuickActionId
import com.aipose.camera.posematch.ui.screens.home.models.HomeUiState
import com.aipose.camera.posematch.ui.vm.HomeViewModel
import com.example.common.showToast
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
    val savedPoseIds by viewModel.savedPoseIds.collectAsStateWithLifecycle()
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

    fun openPoseDetail(pose: Pose) {
        dismissKeyboard()
        navController.navigateOnClick(NavRoute.PoseDetailScreenRoute.routeFor(pose.id))
    }

    fun openAlbum(category: String) {
        dismissKeyboard()
        navController.navigateOnClick(NavRoute.PoseAlbumScreenRoute.routeFor(category))
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

    PoseGlowBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            val content = uiState as? HomeUiState.Content

            item(key = HOME_HEADER_KEY) {
                HomeHeader(
                    streakDays = content?.progress?.dayStreak ?: 0,
                    onOpenPro = {
                        dismissKeyboard()
                        navController.navigateOnClick(NavRoute.ProScreenRoute.route)
                    },
                    modifier = Modifier.statusBarsPadding(),
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

            if (content == null) return@LazyColumn

            val searchResults = content.searchResults
            if (searchResults != null) {
                if (searchResults.isEmpty()) {
                    item(key = HOME_SEARCH_EMPTY_KEY) { PoseSearchEmptyCard() }
                } else {
                    searchResults.chunked(POSE_ROW_SIZE).forEachIndexed { index, rowPoses ->
                        item(key = HOME_SEARCH_ROW_KEY + index) {
                            PoseThumbRow(
                                poses = rowPoses,
                                onPoseClick = ::openPoseDetail,
                                savedPoseIds = savedPoseIds,
                                onToggleSaved = viewModel::toggleSavedPose,
                            )
                        }
                    }
                }
                return@LazyColumn
            }

            item(key = HOME_FILTER_KEY) {
                HomeFilterBar(
                    filter = content.filter,
                    onSelectCategory = viewModel::selectCategory,
                    onSelectDifficulty = viewModel::selectDifficulty,
                    onShuffle = viewModel::shuffle,
                )
            }

            content.hero?.let { hero ->
                item(key = HOME_HERO_KEY) {
                    HomeHeroCard(hero = hero, onStartPosing = { openCamera(hero.pose) })
                }
            }

            item(key = HOME_ACTIONS_KEY) {
                HomeQuickActionGrid(
                    actions = content.quickActions,
                    onActionClick = { action ->
                        when (val id = action.id) {
                            HomeQuickActionId.LivePose -> content.hero?.pose?.let(::openCamera)

                            HomeQuickActionId.Import -> {
                                dismissKeyboard()
                                posePicker()
                            }

                            HomeQuickActionId.Explore ->
                                openAlbum(NavRoute.PoseAlbumScreenRoute.ALL_CATEGORIES)

                            else -> id.category?.let(::openAlbum)
                        }
                    },
                )
            }

            item(key = HOME_PROGRESS_KEY) {
                HomeProgressCard(
                    progress = content.progress,
                    onViewAll = {
                        dismissKeyboard()
                        navController.navigateOnClick(NavRoute.CollectionsScreenRoute.route)
                    },
                )
            }

            content.poseOfTheDay?.let { pose ->
                item(key = HOME_DAILY_KEY) {
                    HomePoseOfTheDayCard(pose = pose, onTryPose = { openPoseDetail(pose) })
                }
            }
        }
    }
}

private const val HOME_HEADER_KEY = "home_header"
private const val HOME_SEARCH_KEY = "home_search"
private const val HOME_SEARCH_EMPTY_KEY = "home_search_empty"
private const val HOME_SEARCH_ROW_KEY = "home_search_row_"
private const val HOME_FILTER_KEY = "home_filter"
private const val HOME_HERO_KEY = "home_hero"
private const val HOME_ACTIONS_KEY = "home_actions"
private const val HOME_PROGRESS_KEY = "home_progress"
private const val HOME_DAILY_KEY = "home_daily"
