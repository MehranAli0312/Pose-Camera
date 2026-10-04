package com.aipose.camera.posematch.ui.screens.saved.models

import androidx.annotation.StringRes
import com.aipose.camera.posematch.R

enum class SavedTab(@StringRes val labelRes: Int, @StringRes val sectionRes: Int) {
    Shots(R.string.saved_tab_shots, R.string.saved_section_shots),
    Poses(R.string.saved_tab_poses, R.string.saved_section_poses)
}
