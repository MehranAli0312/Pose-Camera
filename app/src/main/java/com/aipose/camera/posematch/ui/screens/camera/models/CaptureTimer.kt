package com.aipose.camera.posematch.ui.screens.camera.models

import androidx.annotation.DrawableRes
import com.aipose.camera.posematch.R

enum class CaptureTimer(val seconds: Int, @DrawableRes val iconRes: Int) {
    Off(0, R.drawable.ic_timer_off),
    ThreeSeconds(3, R.drawable.ic_camera_timer),
    FiveSeconds(5, R.drawable.ic_camera_timer),
    TenSeconds(10, R.drawable.ic_camera_timer);

    fun next(): CaptureTimer = entries[(ordinal + 1) % entries.size]

    val isEnabled: Boolean get() = this != Off
}
