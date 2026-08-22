package com.aipose.camera.posematch.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/** Rule-of-thirds grid drawn over the camera preview (toggled via visibility). */
class GridOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint().apply {
        color = Color.argb((0.35f * 255).toInt(), 255, 255, 255)
        strokeWidth = 1.5f * resources.displayMetrics.density
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        canvas.drawLine(w / 3f, 0f, w / 3f, h, paint)
        canvas.drawLine(w * 2f / 3f, 0f, w * 2f / 3f, h, paint)
        canvas.drawLine(0f, h / 3f, w, h / 3f, paint)
        canvas.drawLine(0f, h * 2f / 3f, w, h * 2f / 3f, paint)
    }
}
