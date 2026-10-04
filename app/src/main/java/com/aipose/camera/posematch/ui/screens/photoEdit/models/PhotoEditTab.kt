package com.aipose.camera.posematch.ui.screens.photoEdit.models

import androidx.annotation.StringRes
import com.aipose.camera.posematch.R

enum class PhotoEditTab(@StringRes val labelRes: Int) {
    Filters(R.string.tab_filters),
    Adjust(R.string.tab_adjust),
    Crop(R.string.tab_crop)
}
