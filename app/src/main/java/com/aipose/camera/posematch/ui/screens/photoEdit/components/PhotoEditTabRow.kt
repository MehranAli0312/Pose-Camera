package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import com.aipose.camera.posematch.ui.common.PoseSegmentedTabs
import com.aipose.camera.posematch.ui.screens.photoEdit.models.PhotoEditTab
import com.aipose.camera.posematch.ui.theme.Violet

@Composable
internal fun PhotoEditTabRow(
    selectedTab: PhotoEditTab,
    onTabSelected: (PhotoEditTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    PoseSegmentedTabs(
        labels = PhotoEditTab.entries.map { tab -> stringResource(tab.labelRes) },
        selectedIndex = selectedTab.ordinal,
        pillBrush = SolidColor(Violet),
        onSelect = { index -> onTabSelected(PhotoEditTab.entries[index]) },
        modifier = modifier,
    )
}
