package com.aipose.camera.posematch.ui.screens.poseAlbum.models

import androidx.annotation.StringRes
import com.aipose.camera.posematch.R

enum class PoseSort(@StringRes val labelRes: Int) {
    Featured(R.string.explore_sort_featured),
    NameAsc(R.string.saved_sort_name),
    EasiestFirst(R.string.explore_sort_easiest)
}
