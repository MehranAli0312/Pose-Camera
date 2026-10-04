package com.aipose.camera.posematch.ui.screens.photoEdit.models

import androidx.annotation.StringRes
import com.aipose.camera.posematch.R

enum class CropAspect(@StringRes val labelRes: Int, val ratio: Float?) {
    Free(R.string.crop_free, null),
    Square(R.string.crop_square, 1f),
    FourByFive(R.string.crop_four_by_five, 0.8f),
    ThreeByFour(R.string.crop_three_by_four, 0.75f),
    SixteenByNine(R.string.crop_sixteen_by_nine, 16f / 9f)
}
