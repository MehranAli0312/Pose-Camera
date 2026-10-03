package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.CaptureDraft
import com.aipose.camera.posematch.domain.repo.CaptureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class CaptureUseCase(private val captureRepository: CaptureRepository) {

    val draft: StateFlow<CaptureDraft?> = captureRepository.draft

    fun observeCaptures(): Flow<List<Capture>> = captureRepository.observeCaptures()

    suspend fun save(capture: Capture): Long = captureRepository.save(capture)

    suspend fun delete(capture: Capture) {
        captureRepository.delete(capture.id)
    }

    suspend fun toggleFavorite(capture: Capture) {
        captureRepository.setFavorite(capture.id, !capture.isFavorite)
    }

    suspend fun exportToGallery(capture: Capture): Boolean =
        captureRepository.exportToGallery(capture)

    suspend fun createCaptureTarget(): String = captureRepository.createCaptureTarget()

    suspend fun discardFile(path: String) {
        captureRepository.discardFile(path)
    }

    fun putDraft(draft: CaptureDraft) {
        captureRepository.putDraft(draft)
    }

    fun clearDraft() {
        captureRepository.clearDraft()
    }
}
