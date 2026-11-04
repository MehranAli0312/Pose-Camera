package com.example.ads.internal

import com.example.ads.AdEventType
import com.example.ads.AdResult
import kotlinx.coroutines.CompletableDeferred

internal class AdPresentation(
    private val onShownCallback: () -> Unit = {},
    private val onEvent: (AdEventType) -> Unit = {},
) {

    private val completion = CompletableDeferred<AdResult>()

    @Volatile
    private var reward: AdResult.Rewarded? = null

    fun onShown() {
        onEvent(AdEventType.SHOWN)
        onShownCallback()
    }

    fun onClicked() {
        onEvent(AdEventType.CLICKED)
    }

    fun onReward(type: String, amount: Int) {
        reward = AdResult.Rewarded(type, amount)
    }

    fun onDismissed() {
        completion.complete(reward ?: AdResult.Shown)
    }

    fun onFailed(message: String) {
        onEvent(AdEventType.SHOW_FAILED)
        completion.complete(AdResult.Failed(message))
    }

    suspend fun await(): AdResult = completion.await()
}
