package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.models.NormalizedPoint
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.models.PoseJoint
import com.aipose.camera.posematch.domain.models.PoseMatch
import com.aipose.camera.posematch.domain.pose.PoseMatcher

class PoseMatchUseCase {

    fun match(
        pose: Pose?,
        userLandmarks: Map<PoseJoint, NormalizedPoint>,
        detectedReferenceLandmarks: Map<PoseJoint, NormalizedPoint>
    ): PoseMatch {
        if (pose == null || userLandmarks.isEmpty()) return PoseMatch()
        val reference = detectedReferenceLandmarks
            .takeIf { it.size >= MINIMUM_REFERENCE_LANDMARKS }
            ?: pose.landmarks
        return PoseMatcher.match(userLandmarks, reference)
    }

    fun smoothScore(previousScore: Int, targetScore: Int): Int =
        PoseMatcher.smoothScore(previousScore, targetScore)

    private companion object {
        const val MINIMUM_REFERENCE_LANDMARKS = 4
    }
}
