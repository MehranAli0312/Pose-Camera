package com.aipose.camera.posematch.data.repoImpl

import com.aipose.camera.posematch.data.local.AppDataStore
import com.aipose.camera.posematch.domain.repo.LanguageRepository
import kotlinx.coroutines.flow.Flow

class LanguageRepositoryImpl(
    private val dataStore: AppDataStore
) : LanguageRepository {

    override suspend fun getLanguageCode(): Flow<String> {
        return dataStore.getLanguageCode()
    }

    override suspend fun setLanguageCode(languageCode: String) {
        dataStore.setLanguageCode(languageCode)
    }
}
