package com.aipose.camera.posematch.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/** A FrameLayout that is always as tall as it is wide (1:1). Used for gallery thumbnails. */
class SquareFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}
