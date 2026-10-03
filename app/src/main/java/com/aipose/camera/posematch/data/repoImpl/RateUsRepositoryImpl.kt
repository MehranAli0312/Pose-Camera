package com.aipose.camera.posematch.data.repoImpl

import com.aipose.camera.posematch.data.local.AppDataStore
import com.aipose.camera.posematch.domain.repo.RateUsRepository
import kotlinx.coroutines.flow.first

class RateUsRepositoryImpl(
    private val dataStore: AppDataStore,
) : RateUsRepository {

    override suspend fun isSubmitted(): Boolean = dataStore.getRateUsSubmitted().first()

    override suspend fun setSubmitted(submitted: Boolean) {
        dataStore.setRateUsSubmitted(submitted)
    }

    override suspend fun promptCount(): Int = dataStore.getRateUsPromptCount().first()

    override suspend fun incrementPromptCount() {
        dataStore.incrementRateUsPromptCount()
    }
}
