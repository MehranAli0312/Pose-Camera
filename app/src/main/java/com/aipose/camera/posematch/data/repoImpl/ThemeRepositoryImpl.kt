package com.aipose.camera.posematch.data.repoImpl

import com.aipose.camera.posematch.data.local.AppDataStore
import com.aipose.camera.posematch.domain.models.AppThemeOption
import com.aipose.camera.posematch.domain.repo.ThemeRepository
import kotlinx.coroutines.flow.Flow

class ThemeRepositoryImpl(
    private val dataStore: AppDataStore
) : ThemeRepository {

    override fun getThemeOption(): Flow<AppThemeOption> = dataStore.getThemeOption()

    override suspend fun setThemeOption(option: AppThemeOption) {
        dataStore.setThemeOption(option)
    }
}
