package com.aipose.camera.posematch.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.launch

private const val RESTING_SCALE = 1f
private const val PRESS_DELAY_MILLIS = 100L
private val BounceSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow,
)

internal data class BounceElement(
    val enabled: Boolean,
    val pressedScale: Float,
) : ModifierNodeElement<BounceNode>() {

    override fun create(): BounceNode = BounceNode(enabled, pressedScale)

    override fun update(node: BounceNode) {
        node.update(enabled, pressedScale)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "bounce"
        properties["enabled"] = enabled
        properties["pressedScale"] = pressedScale
    }
}

internal class BounceNode(
    private var enabled: Boolean,
    private var pressedScale: Float,
) : DelegatingNode(), LayoutModifierNode {

    private val scale = Animatable(RESTING_SCALE)

    init {
        delegate(SuspendingPointerInputModifierNode { trackPresses() })
    }

    fun update(enabled: Boolean, pressedScale: Float) {
        this.pressedScale = pressedScale
        if (this.enabled == enabled) return
        this.enabled = enabled
        if (!enabled && isAttached) animateScaleTo(RESTING_SCALE)
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) {
                scaleX = scale.value
                scaleY = scale.value
            }
        }
    }

    private suspend fun PointerInputScope.trackPresses() {
        awaitEachGesture {
            awaitFirstDown()
            if (!enabled) return@awaitEachGesture
            var isCancelled = false
            val earlyUp = withTimeoutOrNull(PRESS_DELAY_MILLIS) {
                waitForUpOrCancellation().also { up -> isCancelled = up == null }
            }
            when {
                isCancelled -> Unit
                earlyUp != null -> bounceOnce()
                else -> {
                    animateScaleTo(pressedScale)
                    waitForUpOrCancellation()
                    animateScaleTo(RESTING_SCALE)
                }
            }
        }
    }

    private fun bounceOnce() {
        coroutineScope.launch {
            scale.animateTo(pressedScale, BounceSpring)
            scale.animateTo(RESTING_SCALE, BounceSpring)
        }
    }

    private fun animateScaleTo(target: Float) {
        coroutineScope.launch { scale.animateTo(target, BounceSpring) }
    }
}
