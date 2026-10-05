package com.aipose.camera.posematch.ui.screens.progress.models

import androidx.annotation.StringRes
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.ProgressPeriod

@get:StringRes
val ProgressPeriod.tabLabelRes: Int
    get() = when (this) {
        ProgressPeriod.Week -> R.string.progress_tab_week
        ProgressPeriod.Month -> R.string.progress_tab_month
        ProgressPeriod.AllTime -> R.string.progress_tab_all_time
    }

@get:StringRes
val ProgressPeriod.trendRangeRes: Int
    get() = when (this) {
        ProgressPeriod.Week -> R.string.progress_trend_week
        ProgressPeriod.Month -> R.string.progress_trend_month
        ProgressPeriod.AllTime -> R.string.progress_trend_all_time
    }

@get:StringRes
val ProgressPeriod.deltaLabelRes: Int?
    get() = when (this) {
        ProgressPeriod.Week -> R.string.progress_delta_week
        ProgressPeriod.Month -> R.string.progress_delta_month
        ProgressPeriod.AllTime -> null
    }
