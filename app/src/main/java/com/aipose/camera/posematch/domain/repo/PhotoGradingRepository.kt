package com.aipose.camera.posematch.domain.repo

import com.aipose.camera.posematch.domain.models.ColorGrade
import com.aipose.camera.posematch.domain.models.PhotoAdjustments
import com.aipose.camera.posematch.domain.models.PhotoFilterId
import com.aipose.camera.posematch.domain.models.ReferenceLook

interface PhotoGradingRepository {

    fun gradeFor(filterId: PhotoFilterId, autoGrade: ColorGrade?): ColorGrade?

    fun gradeFor(adjustments: PhotoAdjustments): ColorGrade?

    fun compose(base: ColorGrade?, overlay: ColorGrade?): ColorGrade?

    suspend fun extractReferenceLook(imagePath: String): ReferenceLook?

    suspend fun writeEditedCopy(
        sourcePath: String,
        grade: ColorGrade?,
        rotationDegrees: Int,
        cropAspect: Float?,
        targetPath: String
    ): Boolean
}
