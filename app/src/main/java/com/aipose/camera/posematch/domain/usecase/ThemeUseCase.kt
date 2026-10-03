package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.models.AppThemeOption
import com.aipose.camera.posematch.domain.repo.ThemeRepository
import kotlinx.coroutines.flow.Flow

class ThemeUseCase(private val themeRepository: ThemeRepository) {

    fun getThemeOption(): Flow<AppThemeOption> = themeRepository.getThemeOption()

    suspend fun setThemeOption(option: AppThemeOption) {
        themeRepository.setThemeOption(option)
    }
}
