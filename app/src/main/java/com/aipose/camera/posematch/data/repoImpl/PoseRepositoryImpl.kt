package com.aipose.camera.posematch.data.repoImpl

import android.content.Context
import android.graphics.Bitmap
import com.aipose.camera.posematch.data.local.ImportedPoseFileDataSource
import com.aipose.camera.posematch.data.local.PoseAssetDataSource
import com.aipose.camera.posematch.data.local.PoseImageDataSource
import com.aipose.camera.posematch.data.local.PoseShareDataSource
import com.aipose.camera.posematch.data.local.dao.CustomPoseDao
import com.aipose.camera.posematch.data.local.entity.CustomPoseEntity
import com.aipose.camera.posematch.data.local.mapper.toDomain
import com.aipose.camera.posematch.data.local.mapper.toFlatLandmarks
import com.aipose.camera.posematch.data.pose.MlKitPoseDetector
import com.aipose.camera.posematch.data.pose.SubjectCutoutDataSource
import com.aipose.camera.posematch.domain.models.NormalizedPoint
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.models.PoseJoint
import com.aipose.camera.posematch.domain.models.PoseSource
import com.aipose.camera.posematch.domain.repo.PoseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class PoseRepositoryImpl(
    private val context: Context,
    private val assetDataSource: PoseAssetDataSource,
    private val importedFileDataSource: ImportedPoseFileDataSource,
    private val imageDataSource: PoseImageDataSource,
    private val shareDataSource: PoseShareDataSource,
    private val customPoseDao: CustomPoseDao,
    private val poseDetector: MlKitPoseDetector,
    private val cutoutDataSource: SubjectCutoutDataSource
) : PoseRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun observePoses(): Flow<List<Pose>> = combine(
        flow { emit(assetDataSource.readTemplates().map { it.toDomain() }) },
        customPoseDao.observeAll()
    ) { bundled, custom ->
        custom.map { entity -> entity.toDomain(decodeLandmarks(entity.landmarksJson)) } + bundled
    }.flowOn(Dispatchers.IO)

    override suspend fun detectLandmarks(imagePath: String): Map<PoseJoint, NormalizedPoint> {
        val bitmap = imageDataSource.decode(imagePath) ?: return emptyMap()
        return poseDetector.detect(bitmap)
    }

    override suspend fun importPose(title: String, sourceUri: String): Pose? {
        val imagePath = importedFileDataSource.copyToAppStorage(sourceUri) ?: return null
        val flatLandmarks = detectLandmarks(imagePath).toFlatLandmarks()
        val entity = CustomPoseEntity(
            title = title,
            imagePath = imagePath,
            category = IMPORTED_CATEGORY,
            difficulty = IMPORTED_DIFFICULTY,
            description = "",
            tags = IMPORTED_TAGS,
            landmarksJson = json.encodeToString(flatLandmarks)
        )
        val id = customPoseDao.insert(entity)
        return entity.copy(id = id).toDomain(flatLandmarks)
    }

    override suspend fun shareableImagePath(pose: Pose): String? =
        shareDataSource.shareableFile(pose.imagePath)?.absolutePath

    override suspend fun cutoutPath(pose: Pose): String? = withContext(Dispatchers.IO) {
        val cached = File(cutoutDirectory(), cutoutFileName(pose))
        if (cached.exists()) return@withContext cached.absolutePath
        val source = imageDataSource.decode(pose.imagePath) ?: return@withContext null
        val cutout = cutoutDataSource.removeBackground(source) ?: return@withContext null
        val written = runCatching {
            cached.outputStream().use { output ->
                cutout.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, output)
            }
        }.getOrDefault(false)
        if (written) cached.absolutePath else null
    }

    private fun decodeLandmarks(landmarksJson: String): Map<String, Float> =
        runCatching { json.decodeFromString<Map<String, Float>>(landmarksJson) }
            .getOrDefault(emptyMap())

    private fun cutoutDirectory(): File =
        File(context.cacheDir, CUTOUT_DIRECTORY).apply { mkdirs() }

    private fun cutoutFileName(pose: Pose): String {
        val prefix = if (pose.source == PoseSource.Imported) IMPORTED_PREFIX else BUNDLED_PREFIX
        return prefix + pose.id + CUTOUT_EXTENSION
    }

    private companion object {
        const val IMPORTED_CATEGORY = "imported"
        const val IMPORTED_DIFFICULTY = "normal"
        const val IMPORTED_TAGS = "custom,imported"
        const val CUTOUT_DIRECTORY = "pose_cutouts"
        const val CUTOUT_EXTENSION = ".png"
        const val BUNDLED_PREFIX = "bundled_"
        const val IMPORTED_PREFIX = "imported_"
        const val PNG_QUALITY = 100
    }
}
