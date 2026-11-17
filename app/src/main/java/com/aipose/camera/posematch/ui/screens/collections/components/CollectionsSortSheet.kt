package com.aipose.camera.posematch.ui.screens.collections.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsFilter
import com.aipose.camera.posematch.ui.common.PoseOptionSheet
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsSort
import com.aipose.camera.posematch.ui.theme.Violet

@Composable
internal fun CollectionsSortSheet(
    selected: CollectionsSort,
    filter: CollectionsFilter,
    onSelect: (CollectionsSort) -> Unit,
    onDismiss: () -> Unit,
) {
    val options = CollectionsSort.entries.filterNot { sort ->
        filter == CollectionsFilter.Favorite && sort == CollectionsSort.HighestMatch
    }
    PoseOptionSheet(
        title = stringResource(
            if (filter == CollectionsFilter.Favorite) {
                R.string.explore_sort_title
            } else {
                R.string.collections_sort_title
            }
        ),
        options = options.map { sort -> stringResource(sort.labelRes) },
        selectedIndex = options.indexOf(selected).coerceAtLeast(0),
        accent = Violet,
        onSelect = { index -> onSelect(options[index]) },
        onDismiss = onDismiss,
    )
}
