package com.aipose.camera.posematch.ui.screens.collections.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.PoseAmber400
import com.aipose.camera.posematch.ui.theme.PoseCyanLight
import com.aipose.camera.posematch.ui.theme.PosePinkSoft

enum class CollectionsFilter(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int?,
    val iconTint: Color?
) {
    All(R.string.collections_filter_all, null, null),
    Favorites(R.string.collections_filter_favorites, R.drawable.ic_pose_heart, PosePinkSoft),
    TopMatch(R.string.collections_filter_top_match, R.drawable.ic_pose_star, PoseAmber400),
    Recent(R.string.collections_filter_recent, R.drawable.ic_camera_timer, PoseCyanLight)
}
