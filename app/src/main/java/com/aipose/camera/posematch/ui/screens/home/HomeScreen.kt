package com.aipose.camera.posematch.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.ads.PremiumRewardedPreloadEffect
import com.aipose.camera.posematch.ads.rememberInnerInterstitial
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.ui.common.PoseGlowBackground
import com.aipose.camera.posematch.ui.common.PoseScreenGutter
import com.aipose.camera.posematch.ui.common.PoseScreenTopSpacing
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.common.safeTopSystemBarsPadding
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.acceptNavigationClick
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.screens.bottomSheet.PremiumFeatureBottomSheet
import com.aipose.camera.posematch.ui.screens.home.components.HomeHeader
import com.aipose.camera.posematch.ui.screens.home.components.HomeHeroCard
import com.aipose.camera.posematch.ui.screens.home.components.HomePoseOfTheDayCard
import com.aipose.camera.posematch.ui.screens.home.components.HomeProgressCard
import com.aipose.camera.posematch.ui.screens.home.components.HomeQuickActionGrid
import com.aipose.camera.posematch.ui.screens.home.models.HomeHero
import com.aipose.camera.posematch.ui.screens.home.models.HomeQuickActionId
import com.aipose.camera.posematch.ui.screens.home.models.HomeDailyPose
import com.aipose.camera.posematch.ui.screens.home.models.HomeUiState
import com.aipose.camera.posematch.ui.screens.home.models.HomeUnlockTarget
import com.aipose.camera.posematch.ui.vm.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showProBadge by viewModel.showProBadge.collectAsStateWithLifecycle()
    val unlockedPoseToOpen by viewModel.unlockedPoseToOpen.collectAsStateWithLifecycle()
    val content = uiState as? HomeUiState.Content
    val innerInterstitial = rememberInnerInterstitial()

    fun openPoseDetail(pose: Pose) {
        navController.navigateOnClick(NavRoute.PoseDetailScreenRoute.routeFor(pose.id))
    }

    fun openWithAd(route: String) {
        if (!navController.acceptNavigationClick()) return
        innerInterstitial.showThen { navController.navigate(route) }
    }

    fun startPosing(pose: Pose) {
        openWithAd(NavRoute.CameraScreenRoute.routeFor(pose.id))
    }

    fun startHeroPosing(hero: HomeHero) {
        if (hero.isLocked) {
            viewModel.showLockedPose(hero.pose, HomeUnlockTarget.Camera)
        } else {
            startPosing(hero.pose)
        }
    }

    fun tryDailyPose(daily: HomeDailyPose) {
        if (daily.isLocked) {
            viewModel.showLockedPose(daily.pose, HomeUnlockTarget.PoseDetail)
        } else {
            openPoseDetail(daily.pose)
        }
    }

    fun openAlbum(category: String) {
        openWithAd(NavRoute.PoseAlbumScreenRoute.routeFor(category))
    }

    LaunchedEffect(unlockedPoseToOpen) {
        val unlocked = unlockedPoseToOpen ?: return@LaunchedEffect
        viewModel.consumeUnlockedPose()
        val route = when (unlocked.target) {
            HomeUnlockTarget.Camera -> NavRoute.CameraScreenRoute.routeFor(unlocked.poseId)
            HomeUnlockTarget.PoseDetail -> NavRoute.PoseDetailScreenRoute.routeFor(unlocked.poseId)
        }
        navController.navigateOnClick(route)
    }

    val hasLockedPose = content?.hero?.isLocked == true || content?.poseOfTheDay?.isLocked == true

    PremiumRewardedPreloadEffect(hasLockedPose)

    PoseGlowBackground {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveWidth()
                .padding(horizontal = PoseScreenGutter),
        ) {
            HomeHeader(
                showProBadge = showProBadge,
                onProClick = { navController.navigateOnClick(NavRoute.ProScreenRoute.route) },
                modifier = Modifier
                    .safeTopSystemBarsPadding()
                    .padding(),
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                if (content == null) return@LazyColumn

                content.hero?.let { hero ->
                    item(key = HOME_HERO_KEY) {
                        HomeHeroCard(
                            hero = hero,
                            onStartPosing = { startHeroPosing(hero) },
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                }

                item(key = HOME_ACTIONS_KEY) {
                    HomeQuickActionGrid(
                        actions = content.quickActions,
                        onActionClick = { action ->
                            when (val id = action.id) {
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

                content.poseOfTheDay?.let { daily ->
                    item(key = HOME_DAILY_KEY) {
                        HomePoseOfTheDayCard(
                            daily = daily,
                            onTryPose = { tryDailyPose(daily) },
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                }
            }
        }
    }

    content?.lockedPose?.let { lockedPose ->
        PremiumFeatureBottomSheet(
            pose = lockedPose,
            onRewardEarned = {
                viewModel.unlockAfterRewardedAd(lockedPose, content.unlockTarget)
            },
            onGoPremiumClick = {
                viewModel.dismissLockedPose()
                navController.navigateOnClick(NavRoute.ProScreenRoute.route)
            },
            onDismissRequest = viewModel::dismissLockedPose,
        )
    }
}

private const val HOME_HERO_KEY = "home_hero"
private const val HOME_ACTIONS_KEY = "home_actions"
private const val HOME_PROGRESS_KEY = "home_progress"
private const val HOME_DAILY_KEY = "home_daily"
