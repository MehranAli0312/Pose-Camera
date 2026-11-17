package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.centerItem
import com.aipose.camera.posematch.ui.screens.poseAlbum.models.PoseCategoryCount
import com.aipose.camera.posematch.ui.theme.PosePremiumGold
import com.aipose.camera.posematch.ui.theme.poseTextStyle

@Composable
internal fun HomePoseCategories(
    categories: List<PoseCategoryCount>,
    selectedCategory: String?,
    onCategorySelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val selectedIndex = categories
        .indexOfFirst { item -> item.category == selectedCategory }
        .takeIf { it >= 0 }
        ?: 0
    val hasCentered = remember { mutableStateOf(false) }

    LaunchedEffect(selectedIndex, categories.size) {
        listState.centerItem(selectedIndex, animated = hasCentered.value)
        hasCentered.value = true
    }

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(top = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(categories, key = { item -> item.category }) { item ->
            HomeCategoryChip(
                label = item.category,
                isSelected = item.category == selectedCategory,
                onClick = { onCategorySelect(item.category) },
            )
        }
    }
}

@Composable
private fun HomeCategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    val backgroundColor = if (isSelected) Color(0xFF2A2016) else Color(0xFF18181F)
    val borderColor = if (isSelected) PosePremiumGold else Color.White.copy(alpha = 0.12f)
    val textColor = if (isSelected) PosePremiumGold else Color.White.copy(alpha = 0.75f)

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = poseTextStyle(
                14.sp,
                if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textColor,
            ),
            maxLines = 1,
        )
    }
}
