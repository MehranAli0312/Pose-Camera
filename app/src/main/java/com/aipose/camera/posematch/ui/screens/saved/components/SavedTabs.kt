package com.aipose.camera.posematch.ui.screens.saved.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import com.aipose.camera.posematch.ui.common.PoseSegmentedTabs
import com.aipose.camera.posematch.ui.screens.saved.models.SavedTab
import com.aipose.camera.posematch.ui.theme.PosePink
import com.aipose.camera.posematch.ui.theme.PosePinkDeep

@Composable
internal fun SavedTabs(
    selected: SavedTab,
    shotsCount: Int,
    posesCount: Int,
    onSelect: (SavedTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    PoseSegmentedTabs(
        labels = listOf(
            stringResource(SavedTab.Shots.labelRes, shotsCount),
            stringResource(SavedTab.Poses.labelRes, posesCount),
        ),
        selectedIndex = selected.ordinal,
        pillBrush = Brush.linearGradient(listOf(PosePink, PosePinkDeep)),
        onSelect = { index -> onSelect(SavedTab.entries[index]) },
        modifier = modifier,
    )
}
