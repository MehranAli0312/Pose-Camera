package com.aipose.camera.posematch.ui.screens.poseAlbum.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseOptionSheet
import com.aipose.camera.posematch.ui.screens.poseAlbum.models.PoseSort
import com.aipose.camera.posematch.ui.theme.Violet

@Composable
internal fun ExploreSortSheet(
    selected: PoseSort,
    onSelect: (PoseSort) -> Unit,
    onDismiss: () -> Unit,
) {
    PoseOptionSheet(
        title = stringResource(R.string.explore_sort_title),
        options = PoseSort.entries.map { sort -> stringResource(sort.labelRes) },
        selectedIndex = selected.ordinal,
        accent = Violet,
        onSelect = { index -> onSelect(PoseSort.entries[index]) },
        onDismiss = onDismiss,
    )
}
