package com.aipose.camera.posematch.ui.screens.home.models

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

sealed interface HomeActionSubtitle {

    data class Label(@StringRes val textRes: Int) : HomeActionSubtitle

    data class Count(@PluralsRes val pluralRes: Int) : HomeActionSubtitle
}
