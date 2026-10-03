package com.aipose.camera.posematch.domain.repo

import kotlinx.coroutines.flow.Flow

interface LanguageRepository {

    suspend fun getLanguageCode(): Flow<String>
    suspend fun setLanguageCode(languageCode: String)

}
