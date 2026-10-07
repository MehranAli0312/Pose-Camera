package com.aipose.camera.posematch.ui.screens.photoEdit.models

import com.aipose.camera.posematch.domain.models.CaptureDraft
import com.aipose.camera.posematch.domain.models.ColorGrade
import com.aipose.camera.posematch.domain.models.PhotoAdjustments
import com.aipose.camera.posematch.domain.models.PhotoFilterId
import com.aipose.camera.posematch.domain.models.PhotoGeometry
import com.aipose.camera.posematch.domain.models.PhotoSize

data class PhotoEditUiState(
    val draft: CaptureDraft? = null,
    val selectedTab: PhotoEditTab = PhotoEditTab.Filters,
    val selectedFilter: PhotoFilterId = PhotoFilterId.Auto,
    val filterIntensity: Float = DEFAULT_INTENSITY,
    val activeTool: AdjustTool = AdjustTool.Exposure,
    val adjustments: PhotoAdjustments = PhotoAdjustments(),
    val geometry: PhotoGeometry = PhotoGeometry(),
    val autoGrade: ColorGrade? = null,
    val autoSwatchColor: Int? = null,
    val activeGrade: ColorGrade? = null,
    val sourceSize: PhotoSize = PhotoSize(0, 0),
    val isComparing: Boolean = false,
    val isSaving: Boolean = false,
    val savedCaptureId: Long? = null
) {
    val outputSize: PhotoSize
        get() = rotatedSourceSize.croppedTo(geometry.cropRect)

    val rotatedSourceSize: PhotoSize
        get() = sourceSize.rotatedBy(geometry.rotationDegrees)

    val previewGrade: ColorGrade?
        get() = if (isComparing) null else activeGrade

    val cropAspect: CropAspect
        get() = CropAspect.forRatio(geometry.cropAspect)

    val activeToolValue: Float get() = adjustments.valueOf(activeTool)

    val matchScore: Int?
        get() = draft?.takeIf { it.poseId != null && it.matchScore > 0 }?.matchScore

    fun isToolTouched(tool: AdjustTool): Boolean = adjustments.valueOf(tool) != 0f

    companion object {
        const val DEFAULT_INTENSITY = 0.8f
    }
}
