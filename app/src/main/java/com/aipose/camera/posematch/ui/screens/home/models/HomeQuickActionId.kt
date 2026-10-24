package com.aipose.camera.posematch.ui.screens.home.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.models.PoseCategories

enum class HomeQuickActionId(
    @DrawableRes val iconRes: Int,
    val palette: GlossyBadgePalette,
    @StringRes val titleRes: Int,
    val category: String?
) {
    Trending(
        iconRes = R.drawable.ic_pose_trending,
        palette = GlossyBadgePalette.Rose,
        titleRes = R.string.home_action_trending,
        category = PoseCategories.VIRAL
    ),
    Couple(
        iconRes = R.drawable.ic_pose_couple,
        palette = GlossyBadgePalette.Pink,
        titleRes = R.string.home_action_couple,
        category = PoseCategories.COUPLE
    ),
    Sunset(
        iconRes = R.drawable.ic_pose_sun,
        palette = GlossyBadgePalette.Amber,
        titleRes = R.string.home_action_sunset,
        category = PoseCategories.SUNSET
    ),
    Nature(
        iconRes = R.drawable.ic_pose_leaf,
        palette = GlossyBadgePalette.Emerald,
        titleRes = R.string.home_action_nature,
        category = PoseCategories.NATURE
    ),
    Mirror(
        iconRes = R.drawable.ic_pose_mirror,
        palette = GlossyBadgePalette.Sky,
        titleRes = R.string.home_action_mirror,
        category = PoseCategories.MIRROR
    ),
    Beach(
        iconRes = R.drawable.ic_pose_beach,
        palette = GlossyBadgePalette.Cyan,
        titleRes = R.string.home_action_beach,
        category = PoseCategories.BEACH
    ),
    Cafe(
        iconRes = R.drawable.ic_pose_cafe,
        palette = GlossyBadgePalette.Orange,
        titleRes = R.string.home_action_cafe,
        category = PoseCategories.CAFE
    ),
    Explore(
        iconRes = R.drawable.ic_pose_explore,
        palette = GlossyBadgePalette.Violet,
        titleRes = R.string.home_action_explore,
        category = null
    )
}
