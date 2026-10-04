package com.aipose.camera.posematch.ui.screens.photoEdit.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.aipose.camera.posematch.R

enum class CropTransform(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
) {
    RotateLeft(R.string.crop_rotate_left, R.drawable.ic_crop_rotate_left),
    RotateRight(R.string.crop_rotate_right, R.drawable.ic_crop_rotate_right),
    FlipHorizontal(R.string.crop_flip_horizontal, R.drawable.ic_crop_flip_horizontal),
    FlipVertical(R.string.crop_flip_vertical, R.drawable.ic_crop_flip_vertical)
}
