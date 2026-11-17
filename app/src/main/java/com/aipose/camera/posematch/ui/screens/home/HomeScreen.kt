package com.aipose.camera.posematch.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ads.rememberHomeInlineNativeAd
import com.aipose.camera.posematch.ads.rememberInnerInterstitial
import com.aipose.camera.posematch.ui.common.PoseGlowBackground
import com.aipose.camera.posematch.ui.common.PoseScreenGutter
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.common.getActivity
import com.aipose.camera.posematch.ui.common.safeTopSystemBarsPadding
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.acceptNavigationClick
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.screens.bottomSheet.ExitPopUp
import com.aipose.camera.posematch.ui.screens.bottomSheet.PremiumFeatureBottomSheet
import com.aipose.camera.posematch.ui.screens.home.components.HomeHeader
import com.aipose.camera.posematch.ui.screens.home.components.HomePoseCategories
import com.aipose.camera.posematch.ui.screens.home.components.homePoseGridItems
import com.aipose.camera.posematch.ui.screens.home.models.HomeUiState
import com.aipose.camera.posematch.ui.screens.poseAlbum.components.ExplorePosePlaceholderCard
import com.aipose.camera.posematch.ui.screens.poseAlbum.models.adaptiveColumnCount
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import com.aipose.camera.posematch.ui.vm.HomeViewModel
import org.koin.androidx.compose.koinViewModel

private val CardMinWidth = 150.dp
private val PoseListSidePadding = 10.dp
private val PoseItemPadding = 5.dp

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showProBadge by viewModel.showProBadge.collectAsStateWithLifecycle()
    val unlockedPoseToOpen by viewModel.unlockedPoseToOpen.collectAsStateWithLifecycle()
    val content = uiState as? HomeUiState.Content
    val homeNativeAd = rememberHomeInlineNativeAd()
    val innerInterstitial = rememberInnerInterstitial()
    val activity = getActivity()
    val showExitDialog = rememberSaveable { mutableStateOf(false) }

    fun openMainSection(route: String) {
        if (!navController.acceptNavigationClick()) return
        innerInterstitial.showThen { navController.navigate(route) }
    }

    BackHandler { showExitDialog.value = true }

    LaunchedEffect(unlockedPoseToOpen) {
        val poseId = unlockedPoseToOpen ?: return@LaunchedEffect
        viewModel.consumeUnlockedPose()
        navController.navigateOnClick(NavRoute.CameraScreenRoute.routeFor(poseId))
    }

    PoseGlowBackground {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveWidth(),
        ) {
            HomeHeader(
                showProBadge = showProBadge,
                onProClick = { navController.navigateOnClick(NavRoute.ProScreenRoute.route) },
                onSettingsClick = { openMainSection(NavRoute.SettingScreenRoute.route) },
                onCollectionsClick = { openMainSection(NavRoute.CollectionsScreenRoute.route) },
                modifier = Modifier
                    .safeTopSystemBarsPadding()
                    .padding(horizontal = PoseScreenGutter),
            )
            if (content != null) {
                HomePoseCategories(
                    categories = content.categories,
                    selectedCategory = content.selectedCategory,
                    onCategorySelect = viewModel::selectCategory,
                    modifier = Modifier.padding(horizontal = PoseScreenGutter),
                )
            }
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val columns = adaptiveColumnCount(
                    availableWidth = maxWidth - PoseListSidePadding * 2,
                    minCellWidth = CardMinWidth,
                    gap = 0.dp,
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = PoseListSidePadding,
                        end = PoseListSidePadding,
                        top = 12.dp,
                        bottom = 24.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    if (content == null) {
                        items(PLACEHOLDER_CARD_COUNT, key = { index -> PLACEHOLDER_KEY + index }) {
                            ExplorePosePlaceholderCard(modifier = Modifier.padding(PoseItemPadding))
                        }
                    } else {
                        if (content.poses.isEmpty()) {
                            item(key = EMPTY_KEY, span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = stringResource(R.string.explore_empty),
                                    style = poseTextStyle(
                                        12.5.sp,
                                        FontWeight.Normal,
                                        LocalAppPalette.current.textMuted,
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                )
                            }
                        } else {
                            homePoseGridItems(
                                poses = content.poses,
                                savedPoseIds = content.savedPoseIds,
                                lockedPoseIds = content.lockedPoseIds,
                                columns = columns,
                                showNativeAd = homeNativeAd.isEnabled,
                                itemPadding = PoseItemPadding,
                                onPoseClick = { pose, isLocked ->
                                    if (isLocked) {
                                        viewModel.showLockedPose(pose)
                                    } else {
                                        navController.navigateOnClick(
                                            NavRoute.CameraScreenRoute.routeFor(pose.id)
                                        )
                                    }
                                },
                                onToggleSaved = viewModel::toggleSaved,
                            )
                        }
                    }
                }
            }
        }
    }

    content?.lockedPose?.let { lockedPose ->
        PremiumFeatureBottomSheet(
            pose = lockedPose,
            onRewardEarned = { viewModel.unlockAfterRewardedAd(lockedPose) },
            onGoPremiumClick = {
                viewModel.dismissLockedPose()
                navController.navigateOnClick(NavRoute.ProScreenRoute.route)
            },
            onDismissRequest = viewModel::dismissLockedPose,
        )
    }

    ExitPopUp(showExitDialog) {
        showExitDialog.value = false
        activity?.finishAffinity()
    }
}

private const val EMPTY_KEY = "home_empty"
private const val PLACEHOLDER_KEY = "home_placeholder_"
private const val PLACEHOLDER_CARD_COUNT = 6
