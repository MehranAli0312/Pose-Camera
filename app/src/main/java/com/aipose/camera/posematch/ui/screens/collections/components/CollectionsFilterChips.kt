package com.aipose.camera.posematch.ui.screens.collections.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.poseGradientPill
import com.aipose.camera.posematch.ui.common.poseRaisedSurface
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsFilter
import com.aipose.camera.posematch.ui.theme.PoseTextBright
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val ChipHeight = 36.dp
private val ChipCorner = 18.dp
private val ChipShape = RoundedCornerShape(ChipCorner)
private val ChipGlyphSize = 14.dp
private const val CHIP_BORDER_ALPHA = 0.1f
private const val SELECTED_SHADOW_ALPHA = 0.45f

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
            FilterChip(
                filter = filter,
                label = if (filter == CollectionsFilter.All && allCount != null) {
                    stringResource(R.string.album_filter_all, allCount)
                } else {
                    stringResource(filter.labelRes)
                },
                isSelected = filter == selected,
                selectedPalette = selectedPalette,
                onClick = { onSelect(filter) },
            )
        }
    }
}

@Composable
private fun FilterChip(
    filter: CollectionsFilter,
    label: String,
    isSelected: Boolean,
    selectedPalette: GlossyBadgePalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val surface = if (isSelected) {
        Modifier.poseGradientPill(
            palette = selectedPalette,
            cornerRadius = ChipCorner,
            shadowAlpha = SELECTED_SHADOW_ALPHA,
            glossInsetX = 5.dp,
            glossTop = 3.dp,
            glossHeight = 16.dp,
        )
    } else {
        Modifier.poseRaisedSurface(ChipShape, borderAlpha = CHIP_BORDER_ALPHA)
    }
    Row(
        modifier = modifier
            .height(ChipHeight)
            .then(surface)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val iconRes = filter.iconRes
        val iconTint = filter.iconTint
        if (iconRes != null && iconTint != null) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(if (isSelected) Color.White else iconTint),
                modifier = Modifier.size(ChipGlyphSize),
            )
        }
        Text(
            text = label,
            style = poseTextStyle(11.sp, FontWeight.Bold, if (isSelected) Color.White else PoseTextBright),
            maxLines = 1,
        )
    }
}
