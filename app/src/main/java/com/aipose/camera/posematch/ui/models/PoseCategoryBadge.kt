package com.aipose.camera.posematch.ui.models

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.aipose.camera.posematch.R

@Immutable
data class PoseCategoryBadge(
    @DrawableRes val iconRes: Int,
    val palette: GlossyBadgePalette
)

private val categoryBadges = mapOf(
    PoseCategories.VIRAL to PoseCategoryBadge(R.drawable.ic_pose_trending, GlossyBadgePalette.Rose),
    PoseCategories.COUPLE to PoseCategoryBadge(R.drawable.ic_pose_couple, GlossyBadgePalette.Pink),
    PoseCategories.SUNSET to PoseCategoryBadge(R.drawable.ic_pose_sun, GlossyBadgePalette.Amber),
    PoseCategories.DARK to PoseCategoryBadge(R.drawable.ic_pose_moon, GlossyBadgePalette.Indigo),
    PoseCategories.MIRROR to PoseCategoryBadge(R.drawable.ic_pose_mirror, GlossyBadgePalette.Sky),
    PoseCategories.BEACH to PoseCategoryBadge(R.drawable.ic_pose_beach, GlossyBadgePalette.Cyan),
    PoseCategories.CAFE to PoseCategoryBadge(R.drawable.ic_pose_cafe, GlossyBadgePalette.Orange),
    PoseCategories.FAMILY to PoseCategoryBadge(R.drawable.ic_pose_family, GlossyBadgePalette.Violet),
    PoseCategories.NATURE to PoseCategoryBadge(R.drawable.ic_pose_leaf, GlossyBadgePalette.Emerald),
    PoseCategories.WATERFALL to PoseCategoryBadge(R.drawable.ic_pose_waterfall, GlossyBadgePalette.Blue),
)

private val fallbackBadge =
    PoseCategoryBadge(R.drawable.ic_pose_explore, GlossyBadgePalette.Violet)

fun badgeForCategory(category: String): PoseCategoryBadge =
    categoryBadges[category] ?: fallbackBadge
