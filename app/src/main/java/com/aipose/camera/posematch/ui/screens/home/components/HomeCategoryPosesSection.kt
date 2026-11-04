package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.centerItem
import com.aipose.camera.posematch.ui.screens.poseAlbum.models.PoseCategoryCount
import com.aipose.camera.posematch.ui.theme.PosePremiumGold
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private const val ALL_CHIP_OFFSET = 1
private const val ALL_CHIP_KEY = "home_cat_all"

@Composable
internal fun HomeCategoryPosesSection(
    categories: List<PoseCategoryCount>,
    selectedCategory: String?,
    poses: List<Pose>,
    savedPoseIds: Set<Int>,
    lockedPoseIds: Set<Int>,
    onCategorySelect: (String?) -> Unit,
    onPoseClick: (pose: Pose, isLocked: Boolean) -> Unit,
    onToggleSaved: (Pose) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val selectedIndex = categories
        .indexOfFirst { item -> item.category == selectedCategory }
        .takeIf { it >= 0 }
        ?.plus(ALL_CHIP_OFFSET)
        ?: 0

    val hasCentered = remember { mutableStateOf(false) }

    LaunchedEffect(selectedIndex, categories.size) {
        listState.centerItem(selectedIndex, animated = hasCentered.value)
        hasCentered.value = true
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.home_poses_title),
                style = poseTextStyle(18.sp, FontWeight.Bold, Color.White),
            )
            Text(
                text = stringResource(R.string.see_all),
                style = poseTextStyle(13.sp, FontWeight.SemiBold, PosePremiumGold),
                modifier = Modifier.bounceClick(onClick = onSeeAllClick),
            )
        }

        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item(key = ALL_CHIP_KEY) {
                CompactCategoryChip(
                    label = stringResource(R.string.collections_filter_all),
                    isSelected = selectedCategory == null,
                    onClick = { onCategorySelect(null) },
                )
            }
            items(categories, key = { item -> item.category }) { item ->
                CompactCategoryChip(
                    label = item.category,
                    isSelected = item.category == selectedCategory,
                    onClick = { onCategorySelect(item.category) },
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(poses, key = { pose -> pose.id }) { pose ->
                val isLocked = pose.id in lockedPoseIds
                HomePoseCard(
                    pose = pose,
                    isSaved = pose.id in savedPoseIds,
                    isLocked = isLocked,
                    onClick = { onPoseClick(pose, isLocked) },
                    onToggleSaved = { onToggleSaved(pose) },
                )
            }
        }
    }
}

@Composable
private fun CompactCategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    val bgColor = if (isSelected) Color(0xFF2A2016) else Color(0xFF18181F)
    val borderColor = if (isSelected) PosePremiumGold else Color.White.copy(alpha = 0.12f)
    val textColor = if (isSelected) PosePremiumGold else Color.White.copy(alpha = 0.75f)

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = poseTextStyle(11.5.sp, FontWeight.SemiBold, textColor),
            maxLines = 1,
        )
    }
}
