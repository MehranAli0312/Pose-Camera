package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.ads.ProStatusStore
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.aipose.camera.posematch.ui.firebaseRemote.FirstSessionPremium
import com.aipose.camera.posematch.ui.firebaseRemote.ReturningSessionPremium
import com.example.ads.ProStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class StartPremiumUseCase(
    private val remoteConfigStore: AdsRemoteConfigStore,
    private val proStatusStore: ProStatusStore,
) {

    suspend fun showAfterSplash(isFirstSession: Boolean): Boolean {
        val config = remoteConfigStore.current
        val enabled = if (isFirstSession) {
            config.firstSessionPremium == FirstSessionPremium.AfterSplash
        } else {
            config.returningSessionPremium == ReturningSessionPremium.AfterSplash
        }
        return enabled && isFreeUser()
    }

    suspend fun showAfterLanguage(): Boolean {
        val config = remoteConfigStore.current
        val enabled = when (config.firstSessionPremium) {
            FirstSessionPremium.AfterLanguage -> true
            FirstSessionPremium.AfterOnboarding -> !config.showOnboardingScreen
            else -> false
        }
        return enabled && isFreeUser()
    }

    suspend fun showAfterOnboarding(): Boolean =
        remoteConfigStore.current.firstSessionPremium == FirstSessionPremium.AfterOnboarding &&
            isFreeUser()

    private suspend fun isFreeUser(): Boolean =
        withTimeoutOrNull(PRO_STATUS_WAIT_MS) {
            proStatusStore.status.first { it.isResolved }
        } == ProStatus.FREE

    private companion object {
        const val PRO_STATUS_WAIT_MS = 2_000L
    }
}
