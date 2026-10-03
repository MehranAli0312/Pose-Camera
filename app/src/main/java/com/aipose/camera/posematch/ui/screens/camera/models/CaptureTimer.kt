package com.aipose.camera.posematch.ui.screens.camera.models

enum class CaptureTimer(val seconds: Int) {
    Off(0),
    ThreeSeconds(3),
    TenSeconds(10);

    fun next(): CaptureTimer = entries[(ordinal + 1) % entries.size]

    val isEnabled: Boolean get() = this != Off
}
