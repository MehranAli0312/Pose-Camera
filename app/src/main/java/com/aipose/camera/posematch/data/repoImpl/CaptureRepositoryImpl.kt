package com.aipose.camera.posematch.data.repoImpl

import com.aipose.camera.posematch.data.local.CaptureDraftStore
import com.aipose.camera.posematch.data.local.CaptureFileDataSource
import com.aipose.camera.posematch.data.local.CaptureGalleryDataSource
import com.aipose.camera.posematch.data.local.dao.CaptureDao
import com.aipose.camera.posematch.data.local.mapper.toDomain
import com.aipose.camera.posematch.data.local.mapper.toEntity
import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.CaptureDraft
import com.aipose.camera.posematch.domain.repo.CaptureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.io.File

class CaptureRepositoryImpl(
    private val captureDao: CaptureDao,
    private val galleryDataSource: CaptureGalleryDataSource,
    private val captureFileDataSource: CaptureFileDataSource,
    private val captureDraftStore: CaptureDraftStore
) : CaptureRepository {

    override val draft: StateFlow<CaptureDraft?> = captureDraftStore.draft

    override fun observeCaptures(): Flow<List<Capture>> =
        captureDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun save(capture: Capture): Long = captureDao.insert(capture.toEntity())

    override suspend fun delete(id: Long) {
        captureDao.deleteById(id)
    }

    override suspend fun setFavorite(id: Long, isFavorite: Boolean) {
        captureDao.setFavorite(id, isFavorite)
    }

    override suspend fun exportToGallery(capture: Capture): Boolean {
        val source = File(capture.imagePath)
        if (!source.exists()) return false
        return galleryDataSource.export(
            source = source,
            albumName = capture.location.name,
            displayName = source.name
        ) != null
    }

    override suspend fun createCaptureTarget(): String = captureFileDataSource.createCaptureTarget()

    override suspend fun discardFile(path: String) {
        captureFileDataSource.delete(path)
    }

    override fun putDraft(draft: CaptureDraft) {
        captureDraftStore.put(draft)
    }

    override fun clearDraft() {
        captureDraftStore.clear()
    }
}
