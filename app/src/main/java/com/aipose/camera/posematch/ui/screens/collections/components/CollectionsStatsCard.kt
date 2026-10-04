package com.aipose.camera.posematch.ui.screens.collections.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseStatsCard
import com.aipose.camera.posematch.ui.models.PoseStat
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsStats
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import com.aipose.camera.posematch.util.bidiIsolate

@Composable
internal fun CollectionsStatsCard(
    stats: CollectionsStats,
    modifier: Modifier = Modifier,
) {
    PoseStatsCard(
        stats = listOf(
            PoseStat(
                value = stats.shots.toString(),
                label = stringResource(R.string.collections_stat_shots),
                valueColor = Color.White,
            ),
            PoseStat(
                value = stats.places.toString(),
                label = stringResource(R.string.collections_stat_places),
                valueColor = Color.White,
            ),
            PoseStat(
                value = stringResource(R.string.score_percent, stats.averageMatch).bidiIsolate(),
                label = stringResource(R.string.collections_stat_average),
                valueColor = PoseVioletLight,
            ),
        ),
        modifier = modifier,
    )
}
