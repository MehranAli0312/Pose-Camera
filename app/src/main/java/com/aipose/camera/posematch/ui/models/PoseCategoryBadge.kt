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
    PoseCategories.MIRROR to PoseCategoryBadge(R.drawable.ic_pose_mirror, GlossyBadgePalette.Sky),
    PoseCategories.NATURE to PoseCategoryBadge(R.drawable.ic_pose_leaf, GlossyBadgePalette.Emerald),
)

private val fallbackBadge =
    PoseCategoryBadge(R.drawable.ic_pose_explore, GlossyBadgePalette.Violet)

fun badgeForCategory(category: String): PoseCategoryBadge =
    categoryBadges[category] ?: fallbackBadge
