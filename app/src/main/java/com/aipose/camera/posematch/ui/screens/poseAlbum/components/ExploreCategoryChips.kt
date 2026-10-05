package com.aipose.camera.posematch.ui.screens.poseAlbum.components

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
import com.aipose.camera.posematch.ui.models.badgeForCategory
import com.aipose.camera.posematch.ui.screens.poseAlbum.models.PoseCategoryCount

private val CategoryGlyphSize = 22.dp

@Composable
internal fun ExploreCategoryChips(
    totalCount: Int,
    categories: List<PoseCategoryCount>,
    selectedCategory: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = ALL_KEY) {
            PoseFilterChip(
                label = stringResource(R.string.album_filter_all, totalCount),
                isSelected = selectedCategory == null,
                onClick = { onSelect(null) },
            )
        }
        items(categories, key = { item -> item.category }) { item ->
            val badge = badgeForCategory(item.category)
            PoseFilterChip(
                label = item.category,
                isSelected = item.category == selectedCategory,
                onClick = { onSelect(item.category) },
                iconRes = badge.iconRes,
                iconTint = badge.palette.mid,
                glyphSize = CategoryGlyphSize,
            )
        }
    }
}

private const val ALL_KEY = "explore_all"
