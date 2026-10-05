package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.CaptureDraft
import com.aipose.camera.posematch.domain.models.PhotoAdjustments
import com.aipose.camera.posematch.domain.models.PhotoFilterId
import com.aipose.camera.posematch.domain.models.PhotoCropRect
import com.aipose.camera.posematch.domain.models.PhotoGeometry
import com.aipose.camera.posematch.domain.usecase.CaptureLocationUseCase
import com.aipose.camera.posematch.domain.usecase.CaptureUseCase
import com.aipose.camera.posematch.domain.usecase.PhotoEditUseCase
import com.aipose.camera.posematch.ui.screens.photoEdit.models.AdjustTool
import com.aipose.camera.posematch.ui.screens.photoEdit.models.CropTransform
import com.aipose.camera.posematch.ui.screens.photoEdit.models.PhotoEditTab
import com.aipose.camera.posematch.ui.screens.photoEdit.models.PhotoEditUiState
import com.aipose.camera.posematch.ui.screens.photoEdit.models.withValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PhotoEditViewModel(
    private val photoEditUseCase: PhotoEditUseCase,
    private val captureUseCase: CaptureUseCase,
    private val captureLocationUseCase: CaptureLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoEditUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeDraft()
    }

    fun selectTab(tab: PhotoEditTab) {
        _uiState.update { state -> state.copy(selectedTab = tab, isComparing = false) }
    }

    fun selectFilter(filterId: PhotoFilterId) {
        _uiState.update { state -> state.withGrade(selectedFilter = filterId) }
    }

    fun setFilterIntensity(intensity: Float) {
        _uiState.update { state -> state.withGrade(filterIntensity = intensity.coerceIn(0f, 1f)) }
    }

    fun selectTool(tool: AdjustTool) {
        _uiState.update { state -> state.copy(activeTool = tool) }
    }

    fun setActiveToolValue(value: Float) {
        _uiState.update { state ->
            state.withGrade(adjustments = state.adjustments.withValue(state.activeTool, value))
        }
    }

    fun resetAdjustments() {
        _uiState.update { state -> state.withGrade(adjustments = PhotoAdjustments()) }
    }

    fun setCropAspect(aspect: Float?) {
        _uiState.update { state ->
            state.copy(geometry = state.geometry.withCrop(aspect, state.cropRectFor(aspect)))
        }
    }

    fun setCropRect(rect: PhotoCropRect) {
        _uiState.update { state -> state.copy(geometry = state.geometry.withCropRect(rect)) }
    }

    fun applyTransform(transform: CropTransform) {
        _uiState.update { state ->
            val geometry = state.geometry
            val transformed = when (transform) {
                CropTransform.RotateLeft -> geometry.rotatedBack()
                CropTransform.RotateRight -> geometry.rotated()
                CropTransform.FlipHorizontal -> geometry.flippedHorizontally()
                CropTransform.FlipVertical -> geometry.flippedVertically()
            }
            val rotatedSize = state.sourceSize.rotatedBy(transformed.rotationDegrees)
            state.copy(
                geometry = transformed.withCropRect(
                    PhotoCropRect.centeredFor(transformed.cropAspect, rotatedSize.aspect ?: 1f)
                )
            )
        }
    }

    fun setStraighten(degrees: Float) {
        _uiState.update { state -> state.copy(geometry = state.geometry.withStraighten(degrees)) }
    }

    fun setComparing(isComparing: Boolean) {
        _uiState.update { state -> state.copy(isComparing = isComparing) }
    }

    fun reset() {
        _uiState.update { state ->
            state.copy(geometry = PhotoGeometry()).withGrade(
                selectedFilter = PhotoFilterId.Original,
                filterIntensity = PhotoEditUiState.DEFAULT_INTENSITY,
                adjustments = PhotoAdjustments(),
            )
        }
    }

    fun discard() {
        val draft = _uiState.value.draft ?: return
        captureUseCase.clearDraft()
        viewModelScope.launch { captureUseCase.discardFile(draft.imagePath) }
    }

    fun save() {
        val state = _uiState.value
        val draft = state.draft ?: return
        if (state.isSaving) return
        _uiState.update { current -> current.copy(isSaving = true, isComparing = false) }
        viewModelScope.launch {
            photoEditUseCase.writeEditedCopy(
                sourcePath = draft.imagePath,
                grade = state.activeGrade,
                geometry = state.geometry,
            )
            val location = captureLocationUseCase.resolveCurrentPlace()
            val capture = Capture(
                imagePath = draft.imagePath,
                capturedAtMillis = System.currentTimeMillis(),
                poseId = draft.poseId,
                matchScore = draft.matchScore,
                category = draft.category,
                title = draft.poseTitle,
                location = location
            )
            val id = captureUseCase.save(capture)
            captureUseCase.exportToGallery(capture.copy(id = id))
            captureUseCase.clearDraft()
            _uiState.update { current -> current.copy(isSaving = false, savedCaptureId = id) }
        }
    }

    fun onSavedCaptureHandled() {
        _uiState.update { state -> state.copy(savedCaptureId = null) }
    }

    private fun observeDraft() {
        viewModelScope.launch {
            captureUseCase.draft.collect { draft ->
                if (draft == null) return@collect
                onDraftReady(draft)
            }
        }
    }

    private suspend fun onDraftReady(draft: CaptureDraft) {
        if (_uiState.value.draft?.imagePath == draft.imagePath) return
        _uiState.value = PhotoEditUiState(draft = draft)
        val sourceSize = photoEditUseCase.readSize(draft.imagePath)
        val referencePath = draft.referenceImagePath ?: draft.imagePath
        val look = photoEditUseCase.extractReferenceLook(referencePath)
        _uiState.update { state ->
            state.copy(
                sourceSize = sourceSize,
                autoGrade = look?.grade,
                autoSwatchColor = look?.dominantColor,
            ).withGrade()
        }
    }

    private fun PhotoEditUiState.cropRectFor(aspect: Float?): PhotoCropRect =
        PhotoCropRect.centeredFor(aspect, rotatedSourceSize.aspect ?: 1f)

    private fun PhotoEditUiState.withGrade(
        selectedFilter: PhotoFilterId = this.selectedFilter,
        filterIntensity: Float = this.filterIntensity,
        adjustments: PhotoAdjustments = this.adjustments,
    ): PhotoEditUiState = copy(
        selectedFilter = selectedFilter,
        filterIntensity = filterIntensity,
        adjustments = adjustments,
        activeGrade = photoEditUseCase.effectiveGrade(
            filterId = selectedFilter,
            autoGrade = autoGrade,
            intensity = filterIntensity,
            adjustments = adjustments,
        ),
    )
}
