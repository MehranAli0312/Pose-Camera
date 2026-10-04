package com.aipose.camera.posematch.ui.screens.camera.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.aipose.camera.posematch.R

enum class CameraTool(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
) {
    Grid(R.string.camera_grid, R.drawable.ic_camera_grid),
    Timer(R.string.camera_timer, R.drawable.ic_camera_timer),
    Skeleton(R.string.camera_skeleton, R.drawable.ic_camera_ratio),
    Pro(R.string.camera_controls, R.drawable.ic_camera_tune)
}
