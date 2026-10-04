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
        get() = sourceSize
            .rotatedBy(geometry.rotationDegrees)
            .croppedTo(geometry.cropAspect)

    val previewGrade: ColorGrade?
        get() = if (isComparing) null else activeGrade

    val isAdjusted: Boolean get() = !adjustments.isNeutral

    fun isToolTouched(tool: AdjustTool): Boolean = adjustments.valueOf(tool) != 0f

    companion object {
        const val DEFAULT_INTENSITY = 0.8f
    }
}
