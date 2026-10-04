package com.aipose.camera.posematch.ui.screens.saved.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.common.click
import com.aipose.camera.posematch.ui.screens.saved.models.SavedTab
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PosePink
import com.aipose.camera.posematch.ui.theme.PosePinkDeep
import com.aipose.camera.posematch.ui.theme.PoseRaisedBottom
import com.aipose.camera.posematch.ui.theme.PoseRaisedTop
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val TrackHeight = 44.dp
private val TrackShape = RoundedCornerShape(22.dp)
private val PillShape = RoundedCornerShape(18.dp)
private val PillInset = 4.dp
private val GlossInset = 5.dp
private val GlossTop = 3.dp
private val GlossHeight = 16.dp

private const val TRACK_BORDER_ALPHA = 0.09f
private const val GLOSS_ALPHA = 0.42f
private const val GLOSS_LAYER_ALPHA = 0.45f
private const val PILL_ANIMATION_MS = 220

@Composable
internal fun SavedTabs(
    selected: SavedTab,
    shotsCount: Int,
    posesCount: Int,
    onSelect: (SavedTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAppPalette.current
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(TrackHeight)
            .clip(TrackShape)
            .background(Brush.verticalGradient(listOf(PoseRaisedTop, PoseRaisedBottom)))
            .border(1.dp, Color.White.copy(alpha = TRACK_BORDER_ALPHA), TrackShape),
    ) {
        val segmentWidth = (maxWidth - PillInset * 2) / SavedTab.entries.size
        val pillOffset by animateFloatAsState(
            targetValue = selected.ordinal.toFloat(),
            animationSpec = tween(PILL_ANIMATION_MS),
            label = "savedTabPill",
        )
        Box(
            modifier = Modifier
                .padding(PillInset)
                .offset(x = segmentWidth * pillOffset)
                .width(segmentWidth)
                .fillMaxSize()
                .clip(PillShape)
                .background(Brush.linearGradient(listOf(PosePink, PosePinkDeep)))
                .drawBehind {
                    val inset = GlossInset.toPx()
                    val top = GlossTop.toPx()
                    val height = GlossHeight.toPx()
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = GLOSS_ALPHA),
                                Color.Transparent,
                            ),
                            startY = top,
                            endY = top + height,
                        ),
                        topLeft = Offset(inset, top),
                        size = Size(size.width - inset * 2f, height),
                        cornerRadius = CornerRadius(height / 2f),
                        alpha = GLOSS_LAYER_ALPHA,
                    )
                },
        )
        Row(modifier = Modifier.fillMaxSize()) {
            TabLabel(
                text = stringResource(SavedTab.Shots.labelRes, shotsCount),
                isSelected = selected == SavedTab.Shots,
                mutedColor = palette.textMuted,
                onClick = { onSelect(SavedTab.Shots) },
                modifier = Modifier.width(segmentWidth + PillInset),
            )
            TabLabel(
                text = stringResource(SavedTab.Poses.labelRes, posesCount),
                isSelected = selected == SavedTab.Poses,
                mutedColor = palette.textMuted,
                onClick = { onSelect(SavedTab.Poses) },
                modifier = Modifier.width(segmentWidth + PillInset),
            )
        }
    }
}

@Composable
private fun TabLabel(
    text: String,
    isSelected: Boolean,
    mutedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .click(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = poseTextStyle(
                12.5.sp,
                FontWeight.Bold,
                if (isSelected) Color.White else mutedColor,
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
