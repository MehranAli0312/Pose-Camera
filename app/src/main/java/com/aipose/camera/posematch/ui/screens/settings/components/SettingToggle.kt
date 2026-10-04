package com.aipose.camera.posematch.ui.screens.settings.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.common.click
import com.aipose.camera.posematch.ui.theme.Indigo
import com.aipose.camera.posematch.ui.theme.PoseGlowPurple

private val TrackWidth = 44.dp
private val TrackHeight = 24.dp
private val KnobRadius = 9.dp
private val KnobInset = 3.dp
private val GlossInset = 3.dp
private val GlossTop = 2.dp
private val GlossHeight = 10.dp

private const val TRACK_OFF_ALPHA = 0.12f
private const val GLOSS_ALPHA = 0.42f
private const val GLOSS_LAYER_ALPHA = 0.5f
private const val KNOB_OFF_ALPHA = 0.55f
private const val TOGGLE_ANIMATION_MS = 180
private const val TOGGLE_LABEL = "settingToggle"

@Composable
internal fun SettingToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(TOGGLE_ANIMATION_MS),
        label = TOGGLE_LABEL,
    )
    Canvas(
        modifier = modifier
            .size(width = TrackWidth, height = TrackHeight)
            .click(onClick = { onCheckedChange(!checked) }),
    ) {
        val corner = CornerRadius(size.height / 2f)
        if (progress < 1f) {
            drawRoundRect(
                color = Color.White.copy(alpha = TRACK_OFF_ALPHA),
                cornerRadius = corner,
            )
        }
        if (progress > 0f) {
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(Indigo, PoseGlowPurple)),
                cornerRadius = corner,
                alpha = progress,
            )
        }

        val glossInset = GlossInset.toPx()
        val glossTop = GlossTop.toPx()
        val glossHeight = GlossHeight.toPx()
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = GLOSS_ALPHA), Color.Transparent),
                startY = glossTop,
                endY = glossTop + glossHeight,
            ),
            topLeft = Offset(glossInset, glossTop),
            size = Size(size.width - glossInset * 2f, glossHeight),
            cornerRadius = CornerRadius(glossHeight / 2f),
            alpha = GLOSS_LAYER_ALPHA,
        )

        val radius = KnobRadius.toPx()
        val travelStart = KnobInset.toPx() + radius
        val travelEnd = size.width - KnobInset.toPx() - radius
        val startX = if (layoutDirection == LayoutDirection.Rtl) travelEnd else travelStart
        val endX = if (layoutDirection == LayoutDirection.Rtl) travelStart else travelEnd
        drawCircle(
            color = Color.White.copy(alpha = KNOB_OFF_ALPHA + (1f - KNOB_OFF_ALPHA) * progress),
            radius = radius,
            center = Offset(startX + (endX - startX) * progress, size.height / 2f),
        )
    }
}
