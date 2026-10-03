package com.aipose.camera.posematch.data.repoImpl

import com.aipose.camera.posematch.data.local.PoseImageDataSource
import com.aipose.camera.posematch.data.pose.CaptureProcessor
import com.aipose.camera.posematch.data.pose.PhotoGradingEngine
import com.aipose.camera.posematch.domain.models.ColorGrade
import com.aipose.camera.posematch.domain.models.PhotoAdjustments
import com.aipose.camera.posematch.domain.models.PhotoFilterId
import com.aipose.camera.posematch.domain.models.ReferenceLook
import com.aipose.camera.posematch.domain.repo.PhotoGradingRepository

class PhotoGradingRepositoryImpl(
    private val imageDataSource: PoseImageDataSource,
    private val gradingEngine: PhotoGradingEngine,
    private val captureProcessor: CaptureProcessor
) : PhotoGradingRepository {

    override fun gradeFor(filterId: PhotoFilterId, autoGrade: ColorGrade?): ColorGrade? =
        gradingEngine.gradeFor(filterId, autoGrade)

    override fun gradeFor(adjustments: PhotoAdjustments): ColorGrade? =
        gradingEngine.adjustmentGrade(adjustments)

    override fun compose(base: ColorGrade?, overlay: ColorGrade?): ColorGrade? =
        gradingEngine.compose(base, overlay)

    override suspend fun extractReferenceLook(imagePath: String): ReferenceLook? {
        val bitmap = imageDataSource.decode(imagePath) ?: return null
        return gradingEngine.extractLook(bitmap)
    }

    override suspend fun writeEditedCopy(
        sourcePath: String,
        grade: ColorGrade?,
        rotationDegrees: Int,
        cropAspect: Float?,
        targetPath: String
    ): Boolean = captureProcessor.writeEditedCopy(
        sourcePath = sourcePath,
        grade = grade,
        rotationDegrees = rotationDegrees,
        cropAspect = cropAspect,
        targetPath = targetPath
    )
}
