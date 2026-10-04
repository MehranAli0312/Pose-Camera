package com.aipose.camera.posematch.domain.repo

import com.aipose.camera.posematch.domain.models.ColorGrade
import com.aipose.camera.posematch.domain.models.PhotoAdjustments
import com.aipose.camera.posematch.domain.models.PhotoFilterId
import com.aipose.camera.posematch.domain.models.PhotoGeometry
import com.aipose.camera.posematch.domain.models.PhotoSize
import com.aipose.camera.posematch.domain.models.ReferenceLook

interface PhotoGradingRepository {
    fun gradeFor(filterId: PhotoFilterId, autoGrade: ColorGrade?): ColorGrade?
    fun gradeFor(adjustments: PhotoAdjustments): ColorGrade?
    fun scale(grade: ColorGrade?, intensity: Float): ColorGrade?
    fun compose(base: ColorGrade?, overlay: ColorGrade?): ColorGrade?
    suspend fun extractReferenceLook(imagePath: String): ReferenceLook?
    suspend fun readSize(imagePath: String): PhotoSize
    suspend fun writeEditedCopy(
        sourcePath: String,
        grade: ColorGrade?,
        geometry: PhotoGeometry,
        targetPath: String
    ): Boolean
}
