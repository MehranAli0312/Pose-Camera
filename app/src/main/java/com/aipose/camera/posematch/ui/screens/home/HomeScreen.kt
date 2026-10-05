package com.aipose.camera.posematch.ui.screens.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.ui.common.PoseGlowBackground
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.common.rememberPosePicker
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.screens.home.components.HomeFilterBar
import com.aipose.camera.posematch.ui.screens.home.components.HomeHeader
import com.aipose.camera.posematch.ui.screens.home.components.HomeHeroCard
import com.aipose.camera.posematch.ui.screens.home.components.HomePoseOfTheDayCard
import com.aipose.camera.posematch.ui.screens.home.components.HomeProgressCard
import com.aipose.camera.posematch.ui.screens.home.components.HomeQuickActionGrid
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
    val importedPose by viewModel.importedPose.collectAsStateWithLifecycle()
    val importFailed by viewModel.importFailed.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val importedMessage = stringResource(R.string.toast_reference_imported)
    val importFailedMessage = stringResource(R.string.toast_reference_failed)
    val importTitle = stringResource(R.string.imported_pose_title)

    val posePicker = rememberPosePicker { pickedUri ->
        viewModel.importPose(importTitle, pickedUri)
    }

    fun openCamera(pose: Pose) {
        navController.navigateOnClick(NavRoute.CameraScreenRoute.routeFor(pose.id))
    }

    fun openPoseDetail(pose: Pose) {
        navController.navigateOnClick(NavRoute.PoseDetailScreenRoute.routeFor(pose.id))
    }

    fun openAlbum(category: String) {
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

    PoseGlowBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            val content = uiState as? HomeUiState.Content

            item(key = HOME_HEADER_KEY) {
                HomeHeader(
                    streakDays = content?.progress?.dayStreak ?: 0,
                    onOpenPro = { navController.navigateOnClick(NavRoute.ProScreenRoute.route) },
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = 8.dp),
                )
            }

            if (content == null) return@LazyColumn

            item(key = HOME_FILTER_KEY) {
                HomeFilterBar(
                    filter = content.filter,
                    onSelectCategory = viewModel::selectCategory,
                    onSelectDifficulty = viewModel::selectDifficulty,
                    onShuffle = viewModel::shuffle,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            content.hero?.let { hero ->
                item(key = HOME_HERO_KEY) {
                    HomeHeroCard(
                        hero = hero,
                        onStartPosing = { openCamera(hero.pose) },
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }

            item(key = HOME_ACTIONS_KEY) {
                HomeQuickActionGrid(
                    actions = content.quickActions,
                    onActionClick = { action ->
                        when (val id = action.id) {
                            HomeQuickActionId.LivePose -> content.hero?.pose?.let(::openCamera)
                            HomeQuickActionId.Import -> posePicker()
                            HomeQuickActionId.Explore ->
                                openAlbum(NavRoute.PoseAlbumScreenRoute.ALL_CATEGORIES)

                            else -> id.category?.let(::openAlbum)
                        }
                    },
                    modifier = Modifier.padding(top = 14.dp),
                )
            }

            item(key = HOME_PROGRESS_KEY) {
                HomeProgressCard(
                    progress = content.progress,
                    onViewAll = { navController.navigateOnClick(NavRoute.ProgressScreenRoute.route) },
                    modifier = Modifier.padding(top = 14.dp),
                )
            }

            content.poseOfTheDay?.let { pose ->
                item(key = HOME_DAILY_KEY) {
                    HomePoseOfTheDayCard(
                        pose = pose,
                        onTryPose = { openPoseDetail(pose) },
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }
    }
}

private const val HOME_HEADER_KEY = "home_header"
private const val HOME_FILTER_KEY = "home_filter"
private const val HOME_HERO_KEY = "home_hero"
private const val HOME_ACTIONS_KEY = "home_actions"
private const val HOME_PROGRESS_KEY = "home_progress"
private const val HOME_DAILY_KEY = "home_daily"
