package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.common.GlossyIconCircle
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.poseRaisedSurface
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.screens.photoEdit.models.AdjustTool
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseTextLavender
import com.aipose.camera.posematch.ui.theme.Violet
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val SlotSize = 58.dp
private val ToolSize = 52.dp
private val RingWidth = 2.5.dp
private val GlyphSize = 17.dp
private val TouchedDotSize = 6.dp
private const val TOOL_BORDER_ALPHA = 0.12f

@Composable
internal fun AdjustToolRail(
    activeTool: AdjustTool,
    isToolTouched: (AdjustTool) -> Boolean,
    onToolSelected: (AdjustTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AdjustTool.entries.forEach { tool ->
            AdjustToolItem(
                tool = tool,
                isActive = tool == activeTool,
                isTouched = isToolTouched(tool),
                onClick = { onToolSelected(tool) },
                modifier = Modifier.weight(1f),
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
    val label = stringResource(tool.labelRes)
    Column(
        modifier = modifier
            .bounceClick(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(SlotSize),
            contentAlignment = Alignment.Center,
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(SlotSize)
                        .border(RingWidth, Violet, CircleShape)
                )
                GlossyIconCircle(
                    iconRes = tool.iconRes,
                    palette = GlossyBadgePalette.Violet,
                    size = ToolSize,
                    glyphSize = GlyphSize,
                    contentDescription = label,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(ToolSize)
                        .poseRaisedSurface(CircleShape, borderAlpha = TOOL_BORDER_ALPHA),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(tool.iconRes),
                        contentDescription = label,
                        colorFilter = ColorFilter.tint(PoseTextLavender),
                        modifier = Modifier.size(GlyphSize),
                    )
                }
            }
            if (isTouched && !isActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                        .size(TouchedDotSize)
                        .clip(CircleShape)
                        .background(Violet)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = poseTextStyle(
                9.5.sp,
                if (isActive) FontWeight.Bold else FontWeight.Normal,
                if (isActive) Color.White else LocalAppPalette.current.textMuted,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
