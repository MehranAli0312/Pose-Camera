package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.CaptureDraft
import com.aipose.camera.posematch.domain.models.ColorGrade
import com.aipose.camera.posematch.domain.models.PhotoAdjustments
import com.aipose.camera.posematch.domain.models.PhotoFilterId
import com.aipose.camera.posematch.domain.usecase.CaptureLocationUseCase
import com.aipose.camera.posematch.domain.usecase.CaptureUseCase
import com.aipose.camera.posematch.domain.usecase.PhotoEditUseCase
import com.aipose.camera.posematch.ui.screens.photoEdit.models.PhotoEditTab
import com.aipose.camera.posematch.ui.screens.photoEdit.models.PhotoEditUiState
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
        _uiState.update { state -> state.copy(selectedTab = tab) }
    }

    fun selectFilter(filterId: PhotoFilterId) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = filterId,
                activeGrade = effectiveGrade(filterId, state.autoGrade, state.adjustments)
            )
        }
    }

    fun updateAdjustments(adjustments: PhotoAdjustments) {
        _uiState.update { state ->
            state.copy(
                adjustments = adjustments,
                activeGrade = effectiveGrade(state.selectedFilter, state.autoGrade, adjustments)
            )
        }
    }

    fun rotate() {
        _uiState.update { state ->
            state.copy(rotationDegrees = (state.rotationDegrees + QUARTER_TURN) % FULL_TURN)
        }
    }

    fun setCropAspect(aspect: Float?) {
        _uiState.update { state ->
            state.copy(cropAspect = if (state.cropAspect == aspect) null else aspect)
        }
    }

    fun reset() {
        _uiState.update { state ->
            state.copy(
                selectedFilter = PhotoFilterId.Original,
                adjustments = PhotoAdjustments(),
                activeGrade = null,
                rotationDegrees = 0,
                cropAspect = null
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
        _uiState.update { current -> current.copy(isSaving = true) }
        viewModelScope.launch {
            photoEditUseCase.writeEditedCopy(
                sourcePath = draft.imagePath,
                grade = state.activeGrade,
                rotationDegrees = state.rotationDegrees,
                cropAspect = state.cropAspect
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
            _uiState.update { current ->
                current.copy(
                    isSaving = false,
                    savedPath = draft.imagePath,
                    savedLocationName = location.name,
                )
            }
        }
    }

    fun previewGrade(filterId: PhotoFilterId): ColorGrade? =
        photoEditUseCase.gradeFor(filterId, _uiState.value.autoGrade)

    fun onSavedPathHandled() {
        _uiState.update { state -> state.copy(savedPath = null) }
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
        val referencePath = draft.referenceImagePath ?: draft.imagePath
        val look = photoEditUseCase.extractReferenceLook(referencePath)
        _uiState.update { state ->
            state.copy(
                autoGrade = look?.grade,
                autoSwatchColor = look?.dominantColor,
                activeGrade = effectiveGrade(state.selectedFilter, look?.grade, state.adjustments)
            )
        }
    }

    private fun effectiveGrade(
        filterId: PhotoFilterId,
        autoGrade: ColorGrade?,
        adjustments: PhotoAdjustments
    ): ColorGrade? = photoEditUseCase.effectiveGrade(filterId, autoGrade, adjustments)

    private companion object {
        const val QUARTER_TURN = 90
        const val FULL_TURN = 360
    }
}
