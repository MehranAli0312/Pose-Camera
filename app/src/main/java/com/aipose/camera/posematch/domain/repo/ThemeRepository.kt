package com.aipose.camera.posematch.domain.repo

import com.aipose.camera.posematch.domain.models.AppThemeOption
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    fun getThemeOption(): Flow<AppThemeOption>
    suspend fun setThemeOption(option: AppThemeOption)
}
