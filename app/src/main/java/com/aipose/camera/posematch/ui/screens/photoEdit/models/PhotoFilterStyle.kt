package com.aipose.camera.posematch.ui.screens.photoEdit.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.PhotoFilterId
import com.aipose.camera.posematch.ui.theme.Indigo
import com.aipose.camera.posematch.ui.theme.PoseAmberLight
import com.aipose.camera.posematch.ui.theme.PoseAmberDeep
import com.aipose.camera.posematch.ui.theme.PoseCyanLight
import com.aipose.camera.posematch.ui.theme.PoseCyanDeep
import com.aipose.camera.posematch.ui.theme.PoseFilterMonoBottom
import com.aipose.camera.posematch.ui.theme.PoseFilterMonoTop
import com.aipose.camera.posematch.ui.theme.PoseFilterPearlBottom
import com.aipose.camera.posematch.ui.theme.PoseFilterPearlTop
import com.aipose.camera.posematch.ui.theme.PoseOrangeDeep
import com.aipose.camera.posematch.ui.theme.PoseOrangeLight
import com.aipose.camera.posematch.ui.theme.PosePinkLight
import com.aipose.camera.posematch.ui.theme.PosePinkDeep
import com.aipose.camera.posematch.ui.theme.PoseRose400
import com.aipose.camera.posematch.ui.theme.PoseRoseDeep
import com.aipose.camera.posematch.ui.theme.PoseSurface
import com.aipose.camera.posematch.ui.theme.PoseSurfaceBorder
import com.aipose.camera.posematch.ui.theme.PoseVioletDeep
import com.aipose.camera.posematch.ui.theme.Violet

data class PhotoFilterStyle(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
    val top: Color,
    val bottom: Color
)

fun photoFilterStyle(filterId: PhotoFilterId): PhotoFilterStyle = when (filterId) {
    PhotoFilterId.Auto -> PhotoFilterStyle(R.string.filter_auto, R.drawable.ic_filter_auto, Indigo, Violet)
    PhotoFilterId.Original ->
        PhotoFilterStyle(R.string.filter_original, R.drawable.ic_filter_original, PoseSurface, PoseSurfaceBorder)

    PhotoFilterId.Vivid -> PhotoFilterStyle(R.string.filter_vivid, R.drawable.ic_filter_vivid, PoseRose400, PoseRoseDeep)
    PhotoFilterId.Golden ->
        PhotoFilterStyle(R.string.filter_golden, R.drawable.ic_filter_golden, PoseAmberLight, PoseAmberDeep)

    PhotoFilterId.Sunrise ->
        PhotoFilterStyle(R.string.filter_sunrise, R.drawable.ic_filter_sunrise, PoseOrangeLight, PoseOrangeDeep)

    PhotoFilterId.Sunset ->
        PhotoFilterStyle(R.string.filter_sunset, R.drawable.ic_filter_sunrise, PoseRose400, PoseOrangeDeep)

    PhotoFilterId.Azure -> PhotoFilterStyle(R.string.filter_azure, R.drawable.ic_filter_golden, PoseCyanLight, PoseCyanDeep)
    PhotoFilterId.Cinema ->
        PhotoFilterStyle(R.string.filter_cinema, R.drawable.ic_filter_vivid, PoseVioletDeep, PoseSurface)

    PhotoFilterId.Fade ->
        PhotoFilterStyle(R.string.filter_fade, R.drawable.ic_filter_original, PoseFilterPearlTop, PoseFilterPearlBottom)

    PhotoFilterId.Noir ->
        PhotoFilterStyle(R.string.filter_noir, R.drawable.ic_filter_original, PoseFilterMonoTop, PoseFilterMonoBottom)

    PhotoFilterId.Vintage ->
        PhotoFilterStyle(R.string.filter_vintage, R.drawable.ic_filter_auto, PosePinkLight, PosePinkDeep)

    PhotoFilterId.Mono ->
        PhotoFilterStyle(R.string.filter_mono, R.drawable.ic_filter_original, PoseFilterMonoTop, PoseFilterMonoBottom)
}
