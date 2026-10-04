package com.aipose.camera.posematch.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val TrackHeight = 44.dp
private val TrackShape = RoundedCornerShape(22.dp)
private val PillShape = RoundedCornerShape(18.dp)
private val PillInset = 4.dp
private val GlossInset = 5.dp
private val GlossTop = 3.dp
private val GlossHeight = 16.dp

private const val GLOSS_ALPHA = 0.42f
private const val GLOSS_LAYER_ALPHA = 0.45f
private const val PILL_ANIMATION_MS = 220

@Composable
fun PoseSegmentedTabs(
    labels: List<String>,
    selectedIndex: Int,
    pillBrush: Brush,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val mutedColor = LocalAppPalette.current.textMuted
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(TrackHeight)
            .poseRaisedSurface(TrackShape),
    ) {
        val segmentWidth = (maxWidth - PillInset * 2) / labels.size.coerceAtLeast(1)
        val pillOffset by animateFloatAsState(
            targetValue = selectedIndex.toFloat(),
            animationSpec = tween(PILL_ANIMATION_MS),
            label = "segmentedTabPill",
        )
        Box(
            modifier = Modifier
                .padding(PillInset)
                .offset(x = segmentWidth * pillOffset)
                .width(segmentWidth)
                .fillMaxSize()
                .clip(PillShape)
                .background(pillBrush)
                .drawBehind { drawPillGloss() },
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PillInset),
        ) {
            labels.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .click { onSelect(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = poseTextStyle(
                            12.5.sp,
                            FontWeight.Bold,
                            if (index == selectedIndex) Color.White else mutedColor,
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawPillGloss() {
    val inset = GlossInset.toPx()
    val top = GlossTop.toPx()
    val height = GlossHeight.toPx()
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = GLOSS_ALPHA), Color.Transparent),
            startY = top,
            endY = top + height,
        ),
        topLeft = Offset(inset, top),
        size = Size(size.width - inset * 2f, height),
        cornerRadius = CornerRadius(height / 2f),
        alpha = GLOSS_LAYER_ALPHA,
    )
}
