package com.aipose.camera.posematch.domain.repo

import com.aipose.camera.posematch.domain.models.AnalyticsEvent
import com.aipose.camera.posematch.domain.models.UserFlow

interface AnalyticsRepository {

    suspend fun userFlow(): UserFlow
    suspend fun markFirstOccurrence(event: AnalyticsEvent): Boolean
    fun logEvent(eventName: String, params: Map<String, String> = emptyMap())

}
