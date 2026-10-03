package com.aipose.camera.posematch.ui.screens.camera.models

enum class CameraFacing {
    Back,
    Front;

    fun toggled(): CameraFacing = if (this == Back) Front else Back

    val isFront: Boolean get() = this == Front
}
