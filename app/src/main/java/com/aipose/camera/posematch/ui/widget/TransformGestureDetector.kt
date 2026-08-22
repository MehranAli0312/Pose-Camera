package com.aipose.camera.posematch.ui.widget

import android.view.MotionEvent
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Reports simultaneous pan / pinch-zoom / two-finger rotation, mirroring Compose's
 * detectTransformGestures. Pan works with one finger; zoom and rotation need two.
 *
 * onTransform(panX, panY, zoom, rotationDeg) is called per move: pan in pixels, zoom as a ratio
 * (1 = no change), rotation in degrees since the previous event.
 */
class TransformGestureDetector(
    private val onTransform: (panX: Float, panY: Float, zoom: Float, rotationDeg: Float) -> Unit
) {
    private var prevFocusX = 0f
    private var prevFocusY = 0f
    private var prevDist = 0f
    private var prevAngle = 0f

    fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_POINTER_UP -> capture(e)

            MotionEvent.ACTION_MOVE -> {
                val (fx, fy) = focus(e)
                val panX = fx - prevFocusX
                val panY = fy - prevFocusY
                var zoom = 1f
                var rotation = 0f
                if (e.pointerCount >= 2) {
                    val dist = distance(e)
                    val angle = angle(e)
                    if (prevDist > 0f) zoom = dist / prevDist
                    rotation = normalize(angle - prevAngle)
                    prevDist = dist
                    prevAngle = angle
                }
                prevFocusX = fx
                prevFocusY = fy
                onTransform(panX, panY, zoom, rotation)
            }
        }
        return true
    }

    private fun capture(e: MotionEvent) {
        val (fx, fy) = focus(e)
        prevFocusX = fx
        prevFocusY = fy
        if (e.pointerCount >= 2) {
            prevDist = distance(e)
            prevAngle = angle(e)
        } else {
            prevDist = 0f
        }
    }

    private fun focus(e: MotionEvent): Pair<Float, Float> {
        var sx = 0f
        var sy = 0f
        val n = e.pointerCount
        for (i in 0 until n) { sx += e.getX(i); sy += e.getY(i) }
        return sx / n to sy / n
    }

    private fun distance(e: MotionEvent) = hypot(e.getX(1) - e.getX(0), e.getY(1) - e.getY(0))

    private fun angle(e: MotionEvent): Float =
        Math.toDegrees(atan2((e.getY(1) - e.getY(0)).toDouble(), (e.getX(1) - e.getX(0)).toDouble())).toFloat()

    private fun normalize(deg: Float): Float {
        var a = deg
        while (a > 180f) a -= 360f
        while (a < -180f) a += 360f
        return a
    }
}
