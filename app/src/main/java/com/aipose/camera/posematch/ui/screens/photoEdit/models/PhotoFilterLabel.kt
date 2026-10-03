package com.aipose.camera.posematch.ui.screens.photoEdit.models

import androidx.annotation.StringRes
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.PhotoFilterId

@StringRes
fun photoFilterLabel(filterId: PhotoFilterId): Int = when (filterId) {
    PhotoFilterId.Auto -> R.string.filter_auto
    PhotoFilterId.Original -> R.string.filter_original
    PhotoFilterId.Vivid -> R.string.filter_vivid
    PhotoFilterId.Golden -> R.string.filter_golden
    PhotoFilterId.Sunrise -> R.string.filter_sunrise
    PhotoFilterId.Sunset -> R.string.filter_sunset
    PhotoFilterId.Azure -> R.string.filter_azure
    PhotoFilterId.Cinema -> R.string.filter_cinema
    PhotoFilterId.Fade -> R.string.filter_fade
    PhotoFilterId.Noir -> R.string.filter_noir
    PhotoFilterId.Vintage -> R.string.filter_vintage
    PhotoFilterId.Mono -> R.string.filter_mono
}
