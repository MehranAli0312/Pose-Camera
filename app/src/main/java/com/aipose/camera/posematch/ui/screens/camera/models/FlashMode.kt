package com.aipose.camera.posematch.ui.screens.camera.models

enum class FlashMode {
    Off,
    On,
    Auto;

    fun next(): FlashMode = when (this) {
        Off -> On
        On -> Auto
        Auto -> Off
    }
}
