package com.aipose.camera.posematch.data.repository

import android.content.Context
import com.aipose.camera.posematch.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface IAppRepository {
    // Preferences Data
    val onboardingCompleted: Flow<Boolean>
    val appTheme: Flow<String>
    val retainSkeleton: Flow<Boolean>

    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setAppTheme(theme: String)
    suspend fun setRetainSkeleton(retain: Boolean)

    // Default Poses
    fun getDefaultPoses(): Flow<List<PoseItem>>

    // Room Historical Captures
    fun getCapturedHistory(): Flow<List<CapturedPhoto>>
    suspend fun saveCapturedPhoto(photo: CapturedPhoto): Long
    suspend fun deleteCapturedPhoto(id: Long)
    suspend fun toggleFavoritePhoto(id: Long, isFav: Boolean)

    // Room Imported/Custom Poses
    fun getCustomPoses(): Flow<List<CustomPose>>
    suspend fun saveCustomPose(pose: CustomPose): Long
    suspend fun deleteCustomPose(id: Long)
    suspend fun renameCustomPose(id: Long, newTitle: String)
}

class AppRepositoryImpl(
    private val appDao: AppDao,
    private val preferencesManager: PreferencesManager,
    private val context: Context
) : IAppRepository {

    override val onboardingCompleted: Flow<Boolean> = preferencesManager.onboardingCompleted
    override val appTheme: Flow<String> = preferencesManager.appTheme
    override val retainSkeleton: Flow<Boolean> = preferencesManager.retainSkeleton

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        preferencesManager.setOnboardingCompleted(completed)
    }

    override suspend fun setAppTheme(theme: String) {
        preferencesManager.setAppTheme(theme)
    }

    override suspend fun setRetainSkeleton(retain: Boolean) {
        preferencesManager.setRetainSkeleton(retain)
    }

    override fun getDefaultPoses(): Flow<List<PoseItem>> = flow {
        emit(DefaultPosesProvider.loadDefaultPoses(context))
    }

    override fun getCapturedHistory(): Flow<List<CapturedPhoto>> = appDao.getAllHistory()

    override suspend fun saveCapturedPhoto(photo: CapturedPhoto): Long {
        return appDao.insertCapturedPhoto(photo)
    }

    override suspend fun deleteCapturedPhoto(id: Long) {
        appDao.deleteCapturedPhoto(id)
    }

    override suspend fun toggleFavoritePhoto(id: Long, isFav: Boolean) {
        appDao.toggleFavoritePhoto(id, isFav)
    }

    override fun getCustomPoses(): Flow<List<CustomPose>> = appDao.getAllCustomPoses()

    override suspend fun saveCustomPose(pose: CustomPose): Long {
        return appDao.insertCustomPose(pose)
    }

    override suspend fun deleteCustomPose(id: Long) {
        appDao.deleteCustomPose(id)
    }

    override suspend fun renameCustomPose(id: Long, newTitle: String) {
        appDao.renameCustomPose(id, newTitle)
    }
}
