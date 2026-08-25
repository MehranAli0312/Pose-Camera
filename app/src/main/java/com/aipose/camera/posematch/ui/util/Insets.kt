package com.aipose.camera.posematch.ui.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.updatePadding

/**
 * Applies the requested system-bar insets (status bar / navigation bar) as padding on top of the
 * view's original padding. Works across all supported API levels together with
 * `enableEdgeToEdge()` in the Activity.
 *
 * Robust for dynamically-added views: many of this app's screens are inflated and attached to the
 * container AFTER the window's first inset pass, so a plain listener would never fire. We capture
 * the original padding once and force a fresh inset dispatch as soon as the view is attached.
 */
fun View.applySystemBarInsets(
    top: Boolean = false,
    bottom: Boolean = false,
    left: Boolean = false,
    right: Boolean = false,
) {
    val baseLeft = paddingLeft
    val baseTop = paddingTop
    val baseRight = paddingRight
    val baseBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.updatePadding(
            left = baseLeft + if (left) bars.left else 0,
            top = baseTop + if (top) bars.top else 0,
            right = baseRight + if (right) bars.right else 0,
            bottom = baseBottom + if (bottom) bars.bottom else 0,
        )
        insets
    }
    // Ensure the listener actually receives insets even when attached after the first pass.
    doOnAttach { ViewCompat.requestApplyInsets(it) }
}
