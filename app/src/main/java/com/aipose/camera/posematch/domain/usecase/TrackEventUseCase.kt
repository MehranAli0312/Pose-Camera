package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.models.AnalyticsEvent
import com.aipose.camera.posematch.domain.repo.AnalyticsRepository

class TrackEventUseCase(private val analyticsRepository: AnalyticsRepository) {

    suspend fun track(event: AnalyticsEvent) {
        val isFirstTime = analyticsRepository.markFirstOccurrence(event)
        val eventName = if (isFirstTime) FIRST_TIME_PREFIX + event.eventName else event.eventName
        analyticsRepository.logEvent(eventName)
    }

    private companion object {
        const val FIRST_TIME_PREFIX = "firstTime_"
    }
}
