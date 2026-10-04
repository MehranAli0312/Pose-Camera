package com.aipose.camera.posematch.ui.screens.collections.models

import androidx.annotation.StringRes
import com.aipose.camera.posematch.R

enum class CollectionsSort(@StringRes val labelRes: Int) {
    Newest(R.string.saved_sort_newest),
    HighestMatch(R.string.saved_sort_highest_match),
    NameAsc(R.string.saved_sort_name)
}
