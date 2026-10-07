package com.aipose.camera.posematch.ui.screens.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import com.aipose.camera.posematch.ui.common.safeBottomSystemBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.ProgressPeriod
import com.aipose.camera.posematch.ui.common.PoseScreenGutter
import com.aipose.camera.posematch.ui.common.PoseTopBar
import com.aipose.camera.posematch.ui.common.poseScreenPadding
import com.aipose.camera.posematch.ui.common.PoseGlowBackground
import com.aipose.camera.posematch.ui.common.PoseGlows
import com.aipose.camera.posematch.ui.common.PoseSegmentedTabs
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.graph.popBackStackOnClick
import com.aipose.camera.posematch.ui.screens.progress.components.ProgressAverageCard
import com.aipose.camera.posematch.ui.screens.progress.components.ProgressCategoriesCard
import com.aipose.camera.posematch.ui.screens.progress.components.ProgressStatTile
import com.aipose.camera.posematch.ui.screens.progress.components.ProgressTrendCard
import com.aipose.camera.posematch.ui.screens.progress.models.ProgressUiState
import com.aipose.camera.posematch.ui.screens.progress.models.deltaLabelRes
import com.aipose.camera.posematch.ui.screens.progress.models.tabLabelRes
import com.aipose.camera.posematch.ui.theme.PoseVioletDeep
import com.aipose.camera.posematch.ui.theme.Violet
import com.aipose.camera.posematch.ui.vm.ProgressViewModel
import com.aipose.camera.posematch.util.bidiIsolate
import org.koin.androidx.compose.koinViewModel

private val TabsPillBrush = Brush.verticalGradient(listOf(Violet, PoseVioletDeep))

@Composable
fun ProgressScreen(
    navController: NavHostController,
    viewModel: ProgressViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val content = uiState as? ProgressUiState.Content
    val period = content?.period ?: ProgressPeriod.Week

    PoseGlowBackground(glows = PoseGlows.Progress) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveWidth(),
        ) {
            PoseTopBar(
                title = stringResource(R.string.progress_title),
                onBack = navController::popBackStackOnClick,
                modifier = Modifier.poseScreenPadding(),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .safeBottomSystemBarsPadding()
                    .padding(
                        start = PoseScreenGutter,
                        end = PoseScreenGutter,
                        top = 16.dp,
                        bottom = 24.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PoseSegmentedTabs(
                    labels = ProgressPeriod.entries.map { stringResource(it.tabLabelRes) },
                    selectedIndex = period.ordinal,
                    pillBrush = TabsPillBrush,
                    onSelect = { index -> viewModel.selectPeriod(ProgressPeriod.entries[index]) },
                    height = 40.dp,
                    labelSize = 12.sp,
                )
                val stats = content?.stats ?: return@Column
                ProgressAverageCard(
                    averageMatch = stats.averageMatch,
                    delta = stats.averageDelta,
                    deltaLabelRes = period.deltaLabelRes,
                )
                ProgressTrendCard(
                    period = period,
                    buckets = stats.trend,
                    averageMatch = stats.averageMatch,
                )
                ProgressCategoriesCard(categories = stats.topCategories)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ProgressStatTile(
                        value = stats.perfectShots.toString(),
                        label = stringResource(R.string.progress_stat_perfect),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    ProgressStatTile(
                        value = stringResource(R.string.progress_streak_value, stats.bestStreak).bidiIsolate(),
                        label = stringResource(R.string.progress_stat_streak),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    ProgressStatTile(
                        value = stats.posesTried.toString(),
                        label = stringResource(R.string.progress_stat_poses),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}
