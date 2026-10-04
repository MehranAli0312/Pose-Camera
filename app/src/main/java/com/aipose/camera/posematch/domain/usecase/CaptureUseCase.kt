package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.CaptureDraft
import com.aipose.camera.posematch.domain.repo.CaptureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow

class CaptureUseCase(private val captureRepository: CaptureRepository) {

    val draft: StateFlow<CaptureDraft?> = captureRepository.draft

    fun observeCaptures(): Flow<List<Capture>> = captureRepository.observeCaptures()

    fun observeCaptureCount(): Flow<Int> =
        captureRepository.observeCaptures().map { captures -> captures.size }

    fun observeCapture(id: Long): Flow<Capture?> =
        captureRepository.observeCaptures().map { captures ->
            captures.firstOrNull { capture -> capture.id == id }
        }

    fun observeBestScore(poseId: Int?): Flow<Int> =
        captureRepository.observeCaptures().map { captures ->
            captures.filter { capture -> poseId == null || capture.poseId == poseId }
                .maxOfOrNull { capture -> capture.matchScore } ?: 0
        }

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
