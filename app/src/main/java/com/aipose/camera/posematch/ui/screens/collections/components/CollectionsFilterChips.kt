package com.aipose.camera.posematch.ui.screens.collections.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseFilterChip
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsFilter

@Composable
internal fun CollectionsFilterChips(
    selected: CollectionsFilter,
    onSelect: (CollectionsFilter) -> Unit,
    modifier: Modifier = Modifier,
    filters: List<CollectionsFilter> = CollectionsFilter.entries,
    selectedPalette: GlossyBadgePalette = GlossyBadgePalette.Violet,
    allCount: Int? = null,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(filters) { filter ->
            PoseFilterChip(
                label = if (filter == CollectionsFilter.All && allCount != null) {
                    stringResource(R.string.album_filter_all, allCount)
                } else {
                    stringResource(filter.labelRes)
                },
                isSelected = filter == selected,
                onClick = { onSelect(filter) },
                iconRes = filter.iconRes,
                iconTint = filter.iconTint,
                selectedPalette = selectedPalette,
            )
        }
    }
}
