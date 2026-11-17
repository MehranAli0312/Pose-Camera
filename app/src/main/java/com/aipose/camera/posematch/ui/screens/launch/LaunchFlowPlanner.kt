package com.aipose.camera.posematch.ui.screens.launch

import com.aipose.camera.posematch.ads.ProStatusStore
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.aipose.camera.posematch.ui.firebaseRemote.FirstSessionPremium
import com.aipose.camera.posematch.ui.firebaseRemote.LanguageScreenMode
import com.aipose.camera.posematch.ui.firebaseRemote.ReturningSessionPremium
import com.aipose.camera.posematch.ui.screens.launch.models.LaunchFlow
import com.aipose.camera.posematch.ui.screens.launch.models.LaunchSession
import com.aipose.camera.posematch.ui.screens.launch.models.LaunchStep
import com.example.ads.ProStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class LaunchFlowPlanner(
    private val store: AdsRemoteConfigStore,
    private val proStatusStore: ProStatusStore,
) {

    suspend fun plan(isFirstSession: Boolean, returningLaunchCount: Int): LaunchFlow {
        store.restore()
        val session = when {
            isFirstSession -> LaunchSession.First
            returningLaunchCount == 1 -> LaunchSession.Second
            else -> LaunchSession.Returning
        }
        val config = store.current
        val includePremium = withTimeoutOrNull(PRO_STATUS_WAIT_MS) {
            proStatusStore.status.first { it.isResolved }
        } == ProStatus.FREE
        val steps = when (session) {
            LaunchSession.First -> buildList {
                if (includePremium && config.firstSessionPremium == FirstSessionPremium.AfterSplash) {
                    add(LaunchStep.Premium)
                }
                if (config.languageScreen == LanguageScreenMode.FirstSession) {
                    add(LaunchStep.Language)
                }
                if (includePremium && config.firstSessionPremium == FirstSessionPremium.AfterLanguage) {
                    add(LaunchStep.Premium)
                }
                if (config.showOnboardingScreen) add(LaunchStep.Onboarding)
                if (includePremium &&
                    config.firstSessionPremium == FirstSessionPremium.AfterOnboarding
                ) {
                    add(LaunchStep.Premium)
                }
                add(LaunchStep.Home)
            }

            LaunchSession.Second -> buildList {
                if (config.languageScreen == LanguageScreenMode.SecondSession) {
                    add(LaunchStep.Language)
                }
                if (includePremium &&
                    config.returningSessionPremium == ReturningSessionPremium.AfterSplash
                ) {
                    add(LaunchStep.Premium)
                }
                add(LaunchStep.Home)
            }

            LaunchSession.Returning -> buildList {
                if (includePremium &&
                    config.returningSessionPremium == ReturningSessionPremium.AfterSplash
                ) {
                    add(LaunchStep.Premium)
                }
                add(LaunchStep.Home)
            }
        }
        return LaunchFlow(session, steps)
    }

    private companion object {
        const val PRO_STATUS_WAIT_MS = 2_000L
    }
}
