package com.aipose.camera.posematch.data.repoImpl

import com.aipose.camera.posematch.data.analytics.AnalyticsDataSource
import com.aipose.camera.posematch.data.local.AppDataStore
import com.aipose.camera.posematch.domain.models.AnalyticsEvent
import com.aipose.camera.posematch.domain.repo.AnalyticsRepository

class AnalyticsRepositoryImpl(
    private val dataStore: AppDataStore,
    private val analyticsDataSource: AnalyticsDataSource,
) : AnalyticsRepository {

    override suspend fun markFirstOccurrence(event: AnalyticsEvent): Boolean =
        dataStore.markAnalyticsEventFirstOccurrence(event.eventName)

    override fun logEvent(eventName: String) {
        analyticsDataSource.logEvent(eventName)
    }
}
