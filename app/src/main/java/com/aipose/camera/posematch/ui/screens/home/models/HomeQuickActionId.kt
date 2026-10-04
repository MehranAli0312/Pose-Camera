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
    val subtitle: HomeActionSubtitle,
    val category: String?
) {
    LivePose(
        iconRes = R.drawable.ic_pose_camera,
        palette = GlossyBadgePalette.Indigo,
        titleRes = R.string.home_action_live_pose,
        subtitle = HomeActionSubtitle.Label(R.string.home_action_open_camera),
        category = null
    ),
    Import(
        iconRes = R.drawable.ic_pose_import,
        palette = GlossyBadgePalette.Orange,
        titleRes = R.string.home_action_import,
        subtitle = HomeActionSubtitle.Label(R.string.home_action_your_photo),
        category = null
    ),
    Trending(
        iconRes = R.drawable.ic_pose_trending,
        palette = GlossyBadgePalette.Rose,
        titleRes = R.string.home_action_trending,
        subtitle = HomeActionSubtitle.Count(R.plurals.home_action_viral_count),
        category = PoseCategories.VIRAL
    ),
    Couple(
        iconRes = R.drawable.ic_pose_couple,
        palette = GlossyBadgePalette.Pink,
        titleRes = R.string.home_action_couple,
        subtitle = HomeActionSubtitle.Count(R.plurals.home_action_pose_count),
        category = PoseCategories.COUPLE
    ),
    Sunset(
        iconRes = R.drawable.ic_pose_sun,
        palette = GlossyBadgePalette.Amber,
        titleRes = R.string.home_action_sunset,
        subtitle = HomeActionSubtitle.Count(R.plurals.home_action_pose_count),
        category = PoseCategories.SUNSET
    ),
    Nature(
        iconRes = R.drawable.ic_pose_leaf,
        palette = GlossyBadgePalette.Emerald,
        titleRes = R.string.home_action_nature,
        subtitle = HomeActionSubtitle.Count(R.plurals.home_action_pose_count),
        category = PoseCategories.NATURE
    ),
    Mirror(
        iconRes = R.drawable.ic_pose_mirror,
        palette = GlossyBadgePalette.Sky,
        titleRes = R.string.home_action_mirror,
        subtitle = HomeActionSubtitle.Count(R.plurals.home_action_pose_count),
        category = PoseCategories.MIRROR
    ),
    Explore(
        iconRes = R.drawable.ic_pose_explore,
        palette = GlossyBadgePalette.Violet,
        titleRes = R.string.home_action_explore,
        subtitle = HomeActionSubtitle.Count(R.plurals.home_action_all_count),
        category = null
    )
}
