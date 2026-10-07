package com.aipose.camera.posematch.domain.models

import com.aipose.camera.posematch.ui.models.PoseCategories

object LockedPoseCatalog {

    private val lockedPositionsByCategory: Map<String, Set<Int>> = mapOf(
        PoseCategories.VIRAL to setOf(2, 4, 6, 7, 10),
        PoseCategories.COUPLE to setOf(3, 6, 8, 10),
        PoseCategories.SUNSET to setOf(1, 4, 6, 10, 14, 18),
        PoseCategories.DARK to setOf(1, 3, 4, 9, 11, 12),
        PoseCategories.MIRROR to setOf(1, 3, 4, 8, 12, 13),
        PoseCategories.BEACH to setOf(2, 3, 6, 9, 10),
        PoseCategories.CAFE to setOf(1, 2, 7, 9, 11),
        PoseCategories.FAMILY to setOf(2, 6, 9, 11),
        PoseCategories.NATURE to setOf(3, 5, 6, 8, 12, 16),
        PoseCategories.WATERFALL to setOf(4, 6, 8, 9, 11),
    )

    fun lockedIds(poses: List<Pose>): Set<Int> = poses
        .filter { pose -> pose.category in lockedPositionsByCategory }
        .groupBy { pose -> pose.category }
        .flatMap { (category, categoryPoses) ->
            val lockedPositions = lockedPositionsByCategory.getValue(category)
            categoryPoses.sortedBy { pose -> pose.id }
                .filterIndexed { index, _ -> index + 1 in lockedPositions }
        }
        .mapTo(mutableSetOf()) { pose -> pose.id }
}
