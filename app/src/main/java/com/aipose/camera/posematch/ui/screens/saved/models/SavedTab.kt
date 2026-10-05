package com.aipose.camera.posematch.ui.screens.saved.models

import androidx.annotation.StringRes
import com.aipose.camera.posematch.R

enum class SavedTab(@StringRes val labelRes: Int) {
    Shots(R.string.saved_tab_shots),
    Poses(R.string.saved_tab_poses)
}
