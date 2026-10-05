package com.aipose.camera.posematch.domain.pose

import com.aipose.camera.posematch.domain.models.NormalizedPoint
import com.aipose.camera.posematch.domain.models.PoseBone
import com.aipose.camera.posematch.domain.models.PoseJoint
import com.aipose.camera.posematch.domain.models.PoseMatch
import kotlin.math.abs
import kotlin.math.atan2

object PoseMatcher {

    private const val DEGREES_TOLERANCE = 75f
    private const val MINIMUM_COMPARABLE_BONES = 3
    private const val SCORE_SMOOTHING_WEIGHT = 0.35f

    fun match(
        userLandmarks: Map<PoseJoint, NormalizedPoint>,
        referenceLandmarks: Map<PoseJoint, NormalizedPoint>
    ): PoseMatch {
        var totalDegrees = 0f
        var comparedBones = 0
        for (bone in PoseBone.entries) {
            val userFrom = userLandmarks[bone.from] ?: continue
            val userTo = userLandmarks[bone.to] ?: continue
            val referenceFrom = referenceLandmarks[bone.from] ?: continue
            val referenceTo = referenceLandmarks[bone.to] ?: continue
            totalDegrees += angleDifference(
                angleOf(userFrom, userTo),
                angleOf(referenceFrom, referenceTo)
            )
            comparedBones++
        }

        if (comparedBones < MINIMUM_COMPARABLE_BONES) return PoseMatch()

        val averageDegrees = totalDegrees / comparedBones
        return PoseMatch(((1f - averageDegrees / DEGREES_TOLERANCE) * 100f).toInt().coerceIn(0, 100))
    }

    fun smoothScore(previousScore: Int, targetScore: Int): Int =
        if (targetScore == 0) 0
        else (SCORE_SMOOTHING_WEIGHT * targetScore + (1f - SCORE_SMOOTHING_WEIGHT) * previousScore)
            .toInt()

    private fun angleOf(from: NormalizedPoint, to: NormalizedPoint): Double =
        atan2((to.y - from.y).toDouble(), (to.x - from.x).toDouble())

    private fun angleDifference(first: Double, second: Double): Float {
        val wrapped = ((Math.toDegrees(first - second).toFloat() % 360f) + 360f) % 360f
        return if (wrapped > 180f) 360f - wrapped else abs(wrapped)
    }
}
