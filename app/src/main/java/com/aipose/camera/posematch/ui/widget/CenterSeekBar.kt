package com.aipose.camera.posematch.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * A compact bidirectional slider whose origin is the CENTER (value 0). Dragging right raises the
 * value toward +1, dragging left lowers it toward -1, and the active track fills outward from the
 * centre — the modern way to present a symmetric adjustment (exposure, contrast, …). Snaps to 0
 * near the middle. Value range is [-1, 1]; [onValueChanged] fires only on user drags.
 */
class CenterSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val d = resources.displayMetrics.density
    private val trackHeight = 4f * d
    private val thumbRadius = 9f * d
    private val notchHeight = 9f * d
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var accentColor: Int = 0xFF6366F1.toInt()
        set(v) { field = v; invalidate() }
    var trackColor: Int = 0x33FFFFFF
    var notchColor: Int = 0x59FFFFFF

    /** Current value in [-1, 1] (0 = centre). Setting it programmatically does not fire the callback. */
    var value: Float = 0f
        set(v) { field = v.coerceIn(-1f, 1f); invalidate() }

    var onValueChanged: ((Float) -> Unit)? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredH = (thumbRadius * 2 + 16f * d).toInt()
        setMeasuredDimension(
            resolveSize((160f * d).toInt(), widthMeasureSpec),
            resolveSize(desiredH, heightMeasureSpec)
        )
    }

    private val trackLeft get() = paddingLeft + thumbRadius
    private val trackRight get() = width - paddingRight - thumbRadius
    private val centerX get() = (trackLeft + trackRight) / 2f
    private val centerY get() = height / 2f
    private fun thumbX() = centerX + value * (trackRight - centerX)

    override fun onDraw(canvas: Canvas) {
        val cy = centerY
        val r = trackHeight / 2f
        // Inactive track.
        paint.color = trackColor
        canvas.drawRoundRect(RectF(trackLeft, cy - r, trackRight, cy + r), r, r, paint)
        // Centre notch.
        paint.color = notchColor
        canvas.drawRoundRect(RectF(centerX - d, cy - notchHeight / 2f, centerX + d, cy + notchHeight / 2f), d, d, paint)
        // Active fill from centre to thumb.
        val tx = thumbX()
        paint.color = accentColor
        canvas.drawRoundRect(RectF(minOf(centerX, tx), cy - r, maxOf(centerX, tx), cy + r), r, r, paint)
        // Thumb (accent disc + white core).
        canvas.drawCircle(tx, cy, thumbRadius, paint)
        paint.color = Color.WHITE
        canvas.drawCircle(tx, cy, thumbRadius * 0.42f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                updateFromTouch(event.x)
                return true
            }
            MotionEvent.ACTION_MOVE -> { updateFromTouch(event.x); return true }
            MotionEvent.ACTION_UP -> { parent?.requestDisallowInterceptTouchEvent(false); performClick(); return true }
            MotionEvent.ACTION_CANCEL -> { parent?.requestDisallowInterceptTouchEvent(false); return true }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean { super.performClick(); return true }

    private fun updateFromTouch(rawX: Float) {
        val half = trackRight - centerX
        if (half <= 0f) return
        val x = rawX.coerceIn(trackLeft, trackRight)
        var v = (x - centerX) / half
        if (abs(v) < 0.04f) v = 0f // snap to centre
        v = v.coerceIn(-1f, 1f)
        if (v != value) {
            value = v
            onValueChanged?.invoke(v)
        }
    }
}
