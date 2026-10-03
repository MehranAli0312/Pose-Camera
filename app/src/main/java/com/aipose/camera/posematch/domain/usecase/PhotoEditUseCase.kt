package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.models.ColorGrade
import com.aipose.camera.posematch.domain.models.PhotoAdjustments
import com.aipose.camera.posematch.domain.models.PhotoFilterId
import com.aipose.camera.posematch.domain.models.ReferenceLook
import com.aipose.camera.posematch.domain.repo.PhotoGradingRepository

class PhotoEditUseCase(private val photoGradingRepository: PhotoGradingRepository) {

    fun gradeFor(filterId: PhotoFilterId, autoGrade: ColorGrade?): ColorGrade? =
        photoGradingRepository.gradeFor(filterId, autoGrade)

    fun effectiveGrade(
        filterId: PhotoFilterId,
        autoGrade: ColorGrade?,
        adjustments: PhotoAdjustments
    ): ColorGrade? = photoGradingRepository.compose(
        photoGradingRepository.gradeFor(filterId, autoGrade),
        photoGradingRepository.gradeFor(adjustments)
    )

    suspend fun extractReferenceLook(imagePath: String): ReferenceLook? =
        photoGradingRepository.extractReferenceLook(imagePath)

    suspend fun writeEditedCopy(
        sourcePath: String,
        grade: ColorGrade?,
        rotationDegrees: Int,
        cropAspect: Float?,
        targetPath: String = sourcePath
    ): Boolean = photoGradingRepository.writeEditedCopy(
        sourcePath = sourcePath,
        grade = grade,
        rotationDegrees = rotationDegrees,
        cropAspect = cropAspect,
        targetPath = targetPath
    )
}
