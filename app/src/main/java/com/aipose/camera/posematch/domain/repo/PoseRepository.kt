package com.aipose.camera.posematch.domain.repo

import com.aipose.camera.posematch.domain.models.NormalizedPoint
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.models.PoseJoint
import kotlinx.coroutines.flow.Flow

interface PoseRepository {

    fun observePoses(): Flow<List<Pose>>

    suspend fun detectLandmarks(imagePath: String): Map<PoseJoint, NormalizedPoint>

    suspend fun importPose(title: String, sourceUri: String): Pose?

    suspend fun cutoutPath(pose: Pose): String?
}
