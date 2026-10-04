package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.GlossyIconBadge
import com.aipose.camera.posematch.ui.common.GlossyIconCircle
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.click
import com.aipose.camera.posematch.ui.common.poseCard
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.screens.home.models.HomeFilter
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseTextChevron
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val BarShape = RoundedCornerShape(22.dp)
private val BarHeight = 68.dp
private val SlotIconSize = 24.dp
private val ShuffleSize = 46.dp

@Composable
internal fun HomeFilterBar(
    filter: HomeFilter,
    onSelectCategory: (String) -> Unit,
    onSelectDifficulty: (String) -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight)
                .poseCard(BarShape)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterSlot(
                iconRes = R.drawable.ic_pose_flame,
                palette = GlossyBadgePalette.Orange,
                labelRes = R.string.home_filter_category,
                value = filter.selectedCategory,
                options = filter.categories,
                onSelect = onSelectCategory,
            )
            Spacer(modifier = Modifier.width(ShuffleSize))
            FilterSlot(
                iconRes = R.drawable.ic_pose_bolt,
                palette = GlossyBadgePalette.Emerald,
                labelRes = R.string.home_filter_level,
                value = filter.selectedDifficulty,
                options = filter.difficulties,
                onSelect = onSelectDifficulty,
            )
        }
        GlossyIconCircle(
            iconRes = R.drawable.ic_pose_shuffle,
            palette = GlossyBadgePalette.Shuffle,
            size = ShuffleSize,
            contentDescription = stringResource(R.string.home_shuffle),
            modifier = Modifier.bounceClick(onClick = onShuffle),
        )
    }
}

@Composable
private fun RowScope.FilterSlot(
    iconRes: Int,
    palette: GlossyBadgePalette,
    labelRes: Int,
    value: String?,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.weight(1f)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .click(enabled = options.isNotEmpty()) { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GlossyIconBadge(iconRes = iconRes, palette = palette, size = SlotIconSize)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(labelRes),
                    style = poseTextStyle(8.5.sp, FontWeight.Bold, LocalAppPalette.current.textFaint),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value.orEmpty(),
                    style = poseTextStyle(15.sp, FontWeight.Bold, Color.White),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Image(
                painter = painterResource(R.drawable.ic_pose_chevron_down),
                contentDescription = null,
                colorFilter = ColorFilter.tint(PoseTextChevron),
                modifier = Modifier.size(width = 12.dp, height = 8.dp),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            style = poseTextStyle(14.sp, FontWeight.SemiBold, Color.White),
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}
