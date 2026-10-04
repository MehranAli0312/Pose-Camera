package com.aipose.camera.posematch.ui.screens.saved.models

import androidx.annotation.StringRes
import com.aipose.camera.posematch.R

enum class SavedSort(@StringRes val labelRes: Int, val shotsOnly: Boolean) {
    Newest(R.string.saved_sort_newest, false),
    HighestMatch(R.string.saved_sort_highest_match, true),
    NameAsc(R.string.saved_sort_name, false)
}
