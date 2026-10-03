package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.screens.photoEdit.models.PhotoEditTab
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

private val TabShape = RoundedCornerShape(10.dp)

@Composable
internal fun PhotoEditTabRow(
    selectedTab: PhotoEditTab,
    onTabSelected: (PhotoEditTab) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAppPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .clip(TabShape)
                .background(palette.card)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            PhotoEditTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                Box(
                    modifier = Modifier
                        .clip(TabShape)
                        .background(if (isSelected) palette.accent else Color.Transparent)
                        .bounceClick(onClick = { onTabSelected(tab) })
                        .padding(horizontal = 20.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(tab.labelRes),
                        color = if (isSelected) Color.White else Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(palette.card)
                .bounceClick(onClick = onReset),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.action_reset),
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
