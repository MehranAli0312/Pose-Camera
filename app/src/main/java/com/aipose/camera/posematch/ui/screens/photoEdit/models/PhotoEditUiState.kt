package com.aipose.camera.posematch.ui.screens.photoEdit.models

import com.aipose.camera.posematch.domain.models.CaptureDraft
import com.aipose.camera.posematch.domain.models.ColorGrade
import com.aipose.camera.posematch.domain.models.PhotoAdjustments
import com.aipose.camera.posematch.domain.models.PhotoFilterId

data class PhotoEditUiState(
    val draft: CaptureDraft? = null,
    val selectedTab: PhotoEditTab = PhotoEditTab.Filters,
    val selectedFilter: PhotoFilterId = PhotoFilterId.Auto,
    val adjustments: PhotoAdjustments = PhotoAdjustments(),
    val autoGrade: ColorGrade? = null,
    val autoSwatchColor: Int? = null,
    val activeGrade: ColorGrade? = null,
    val rotationDegrees: Int = 0,
    val cropAspect: Float? = null,
    val isSaving: Boolean = false,
    val savedPath: String? = null,
    val savedLocationName: String? = null
) {
    fun isToolTouched(tool: AdjustTool): Boolean = when (tool) {
        AdjustTool.Rotate -> rotationDegrees != 0
        AdjustTool.Crop -> cropAspect != null
        else -> adjustments.valueOf(tool) != 0f
    }
}
