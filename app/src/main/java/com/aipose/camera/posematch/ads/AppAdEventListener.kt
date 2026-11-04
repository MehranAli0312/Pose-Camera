package com.aipose.camera.posematch.ads

import com.aipose.camera.posematch.domain.models.AdAnalyticsAction
import com.aipose.camera.posematch.domain.models.AdAnalyticsEvent
import com.aipose.camera.posematch.domain.usecase.TrackEventUseCase
import com.example.ads.AdEvent
import com.example.ads.AdEventListener
import com.example.ads.AdEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppAdEventListener(private val trackEventUseCase: TrackEventUseCase) : AdEventListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onAdEvent(event: AdEvent) {
        val analyticsEvent = AdAnalyticsEvent(
            action = event.type.toAnalyticsAction(),
            format = event.format.name.lowercase(),
            placement = event.placement.id,
        )
        scope.launch { trackEventUseCase.trackAd(analyticsEvent) }
    }

    private fun AdEventType.toAnalyticsAction(): AdAnalyticsAction = when (this) {
        AdEventType.LOADED -> AdAnalyticsAction.Loaded
        AdEventType.LOAD_FAILED -> AdAnalyticsAction.LoadFailed
        AdEventType.SHOWN -> AdAnalyticsAction.Shown
        AdEventType.SHOW_FAILED -> AdAnalyticsAction.ShowFailed
        AdEventType.CLICKED -> AdAnalyticsAction.Clicked
    }
}
