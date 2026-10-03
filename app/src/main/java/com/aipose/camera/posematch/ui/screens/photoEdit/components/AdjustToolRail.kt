package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.screens.photoEdit.models.AdjustTool
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

private val RailItemShape = RoundedCornerShape(10.dp)

@Composable
internal fun AdjustToolRail(
    activeTool: AdjustTool,
    isToolTouched: (AdjustTool) -> Boolean,
    onToolSelected: (AdjustTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(AdjustTool.entries) { tool ->
            AdjustToolItem(
                tool = tool,
                isActive = tool == activeTool,
                isTouched = isToolTouched(tool),
                onClick = { onToolSelected(tool) },
            )
        }
    }
}

@Composable
private fun AdjustToolItem(
    tool: AdjustTool,
    isActive: Boolean,
    isTouched: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAppPalette.current.accent
    val label = stringResource(tool.labelRes)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(58.dp)
            .clip(RailItemShape)
            .background(if (isActive) accent.copy(alpha = 0.15f) else Color.Transparent)
            .bounceClick(onClick = onClick)
            .padding(vertical = 6.dp),
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = tool.icon,
                contentDescription = label,
                tint = when {
                    isActive -> accent
                    isTouched -> Color.White
                    else -> Color.Gray
                },
                modifier = Modifier.size(24.dp),
            )
            if (isTouched) {
                Box(
                    modifier = Modifier
                        .offset(x = 3.dp, y = (-2).dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = if (isActive) accent else Color.Gray,
            fontSize = 9.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
