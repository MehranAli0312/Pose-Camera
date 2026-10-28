package com.aipose.camera.posematch.domain.repo

import com.aipose.camera.posematch.domain.models.AnalyticsEvent

interface AnalyticsRepository {

    suspend fun markFirstOccurrence(event: AnalyticsEvent): Boolean
    fun logEvent(eventName: String)

}
