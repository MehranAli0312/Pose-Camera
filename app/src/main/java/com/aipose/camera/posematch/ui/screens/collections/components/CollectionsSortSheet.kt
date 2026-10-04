package com.aipose.camera.posematch.ui.screens.collections.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseOptionSheet
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsSort
import com.aipose.camera.posematch.ui.theme.Violet

@Composable
internal fun CollectionsSortSheet(
    selected: CollectionsSort,
    onSelect: (CollectionsSort) -> Unit,
    onDismiss: () -> Unit,
) {
    PoseOptionSheet(
        title = stringResource(R.string.collections_sort_title),
        options = CollectionsSort.entries.map { sort -> stringResource(sort.labelRes) },
        selectedIndex = selected.ordinal,
        accent = Violet,
        onSelect = { index -> onSelect(CollectionsSort.entries[index]) },
        onDismiss = onDismiss,
    )
}
