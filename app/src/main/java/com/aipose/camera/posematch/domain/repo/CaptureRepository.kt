package com.aipose.camera.posematch.domain.repo

import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.CaptureDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CaptureRepository {

    val draft: StateFlow<CaptureDraft?>

    fun observeCaptures(): Flow<List<Capture>>

    suspend fun save(capture: Capture): Long

    suspend fun delete(id: Long)

    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    suspend fun exportToGallery(capture: Capture): Boolean

    suspend fun createCaptureTarget(): String

    suspend fun discardFile(path: String)

    fun putDraft(draft: CaptureDraft)

    fun clearDraft()
}
