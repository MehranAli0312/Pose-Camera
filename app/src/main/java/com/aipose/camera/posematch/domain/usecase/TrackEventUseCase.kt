package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.models.AdAnalyticsEvent
import com.aipose.camera.posematch.domain.models.AnalyticsEvent
import com.aipose.camera.posematch.domain.models.PaywallAnalyticsEvent
import com.aipose.camera.posematch.domain.models.UserFlow
import com.aipose.camera.posematch.domain.repo.AnalyticsRepository

class TrackEventUseCase(private val analyticsRepository: AnalyticsRepository) {

    suspend fun track(event: AnalyticsEvent) {
        val userFlow = analyticsRepository.userFlow()
        val isFirstTime = userFlow == UserFlow.FirstTime &&
            analyticsRepository.markFirstOccurrence(event)
        val eventName = if (isFirstTime) FIRST_TIME_PREFIX + event.eventName else event.eventName
        analyticsRepository.logEvent(eventName, mapOf(USER_FLOW_PARAM to userFlow.value))
    }

    suspend fun trackAd(event: AdAnalyticsEvent) {
        val params = mapOf(
            AD_FORMAT_PARAM to event.format,
            AD_PLACEMENT_PARAM to event.placement,
            USER_FLOW_PARAM to analyticsRepository.userFlow().value,
        )
        analyticsRepository.logEvent(event.action.eventName, params)
    }

    suspend fun trackPaywall(event: PaywallAnalyticsEvent) {
        val params = buildMap {
            put(USER_FLOW_PARAM, analyticsRepository.userFlow().value)
            event.plan?.let { put(PLAN_PARAM, it.productId) }
        }
        analyticsRepository.logEvent(event.action.eventName, params)
    }

    private companion object {
        const val FIRST_TIME_PREFIX = "firstTime_"
        const val USER_FLOW_PARAM = "user_flow"
        const val AD_FORMAT_PARAM = "ad_format"
        const val AD_PLACEMENT_PARAM = "ad_placement"
        const val PLAN_PARAM = "plan"
    }
}
