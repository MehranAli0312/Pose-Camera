package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.models.NormalizedPoint
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.models.PoseJoint
import com.aipose.camera.posematch.domain.repo.PoseRepository
import kotlinx.coroutines.flow.Flow

class PoseLibraryUseCase(private val poseRepository: PoseRepository) {

    fun observePoses(): Flow<List<Pose>> = poseRepository.observePoses()

    suspend fun detectLandmarks(imagePath: String): Map<PoseJoint, NormalizedPoint> =
        poseRepository.detectLandmarks(imagePath)

    suspend fun importPose(title: String, sourceUri: String): Pose? =
        poseRepository.importPose(title, sourceUri)

    suspend fun cutoutPath(pose: Pose): String? = poseRepository.cutoutPath(pose)
}
