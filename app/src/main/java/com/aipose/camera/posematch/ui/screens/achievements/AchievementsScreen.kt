package com.aipose.camera.posematch.ui.screens.achievements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import com.aipose.camera.posematch.ui.common.safeBottomSystemBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseScreenGutter
import com.aipose.camera.posematch.ui.common.PoseTopBar
import com.aipose.camera.posematch.ui.common.poseScreenPadding
import com.aipose.camera.posematch.ui.common.PoseGlowBackground
import com.aipose.camera.posematch.ui.common.PoseGlows
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.graph.popBackStackOnClick
import com.aipose.camera.posematch.ui.screens.achievements.components.AchievementBadgeGrid
import com.aipose.camera.posematch.ui.screens.achievements.components.AchievementsNextGoalCard
import com.aipose.camera.posematch.ui.screens.achievements.components.AchievementsStreakCard
import com.aipose.camera.posematch.ui.screens.achievements.models.AchievementsUiState
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import com.aipose.camera.posematch.ui.vm.AchievementsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AchievementsScreen(
    navController: NavHostController,
    viewModel: AchievementsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PoseGlowBackground(glows = PoseGlows.Achievements) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveWidth(),
        ) {
            PoseTopBar(
                title = stringResource(R.string.achievements_title),
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
                        bottom = 24.dp,
                    ),
            ) {
                val summary = (uiState as? AchievementsUiState.Content)?.summary ?: return@Column
                AchievementsStreakCard(
                    currentStreak = summary.currentStreak,
                    bestStreak = summary.bestStreak,
                    week = summary.week,
                    modifier = Modifier.padding(top = 18.dp),
                )
                Text(
                    text = stringResource(
                        R.string.achievements_badges_label,
                        summary.unlockedCount,
                        summary.badges.size,
                    ),
                    style = poseTextStyle(9.sp, FontWeight.Bold, LocalAppPalette.current.textFaint),
                    modifier = Modifier.padding(top = 24.dp, bottom = 12.dp),
                )
                AchievementBadgeGrid(badges = summary.badges)
                summary.nextStreakGoal?.let { goal ->
                    AchievementsNextGoalCard(
                        goal = goal,
                        modifier = Modifier.padding(top = 20.dp),
                    )
                }
            }
        }
    }
}
