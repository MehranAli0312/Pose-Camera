package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.repo.LanguageRepository
import kotlinx.coroutines.flow.Flow

class LanguageUseCase(private val languageRepository: LanguageRepository) {

    suspend fun getLanguageCode(): Flow<String> {
        return languageRepository.getLanguageCode()
    }

    suspend fun setLanguageCode(languageCode: String) {
        languageRepository.setLanguageCode(languageCode)
    }

}
