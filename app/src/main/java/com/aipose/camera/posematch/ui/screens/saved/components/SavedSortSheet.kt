package com.aipose.camera.posematch.ui.screens.saved.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseOptionSheet
import com.aipose.camera.posematch.ui.screens.saved.models.SavedSort
import com.aipose.camera.posematch.ui.screens.saved.models.SavedTab
import com.aipose.camera.posematch.ui.theme.PosePink

@Composable
internal fun SavedSortSheet(
    selected: SavedSort,
    tab: SavedTab,
    onSelect: (SavedSort) -> Unit,
    onDismiss: () -> Unit,
) {
    val options = SavedSort.entries.filterNot { it.shotsOnly && tab == SavedTab.Poses }
    PoseOptionSheet(
        title = stringResource(R.string.saved_sort_title),
        options = options.map { option -> stringResource(option.labelRes) },
        selectedIndex = options.indexOf(selected),
        accent = PosePink,
        onSelect = { index -> onSelect(options[index]) },
        onDismiss = onDismiss,
    )
}
