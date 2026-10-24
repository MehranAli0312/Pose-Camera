package com.aipose.camera.posematch.ui.screens.splash

import android.annotation.SuppressLint
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ads.HomeScreenBottom
import com.aipose.camera.posematch.ads.LanguageScreenBottom
import com.aipose.camera.posematch.ads.MissedSplashAd
import com.aipose.camera.posematch.ads.OnboardScreenBottom
import com.aipose.camera.posematch.ads.SplashAdLog
import com.aipose.camera.posematch.ads.SplashAdTiming
import com.aipose.camera.posematch.ads.SplashFullscreen
import com.aipose.camera.posematch.ads.prepareSplashAd
import com.aipose.camera.posematch.ads.rememberScreenAds
import com.aipose.camera.posematch.data.local.NetworkConnectivityChecker
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote
import com.aipose.camera.posematch.ui.screens.pro.models.StartPremiumNextStep
import com.aipose.camera.posematch.ui.screens.splash.components.SplashAmbientBackground
import com.aipose.camera.posematch.ui.screens.splash.components.SplashProgressBar
import com.aipose.camera.posematch.ui.vm.SplashViewModel
import com.example.ads.AdPlacement
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun SplashScreen(
    viewModel: SplashViewModel = koinViewModel(), navParentController: NavHostController
) {
    val progress = remember { Animatable(0f) }
    val ads = rememberScreenAds()
    val appFirebaseRemote: AppFirebaseRemote = koinInject()
    val network: NetworkConnectivityChecker = koinInject()
    val remoteConfigStore: AdsRemoteConfigStore = koinInject()
    val missedSplashAd: MissedSplashAd = koinInject()

    BackHandler(enabled = true) {}

    LaunchedEffect(Unit) {
        val startedAt = SystemClock.elapsedRealtime()
        val maxWaitMs = remoteConfigStore.current.splashAdMaxWaitMs
        val waitLimitMs = maxWaitMs + SplashAdTiming.AD_LOAD_GRACE_MS
        SplashAdLog.d(
            "splash start: maxWaitMs=$maxWaitMs waitLimitMs=$waitLimitMs " +
                "splashInterstitial=${remoteConfigStore.current.splashInterstitial} " +
                "loadedAlready=${ads.isLoadedOnDemand(AdPlacement.SplashFullscreen)}",
        )

        val creep = launch {
            progress.animateTo(
                targetValue = SplashAdTiming.PROGRESS_CREEP_TARGET,
                animationSpec = tween(
                    durationMillis = waitLimitMs.toInt(),
                    easing = LinearEasing,
                ),
            )
        }

        val isFirstSession = viewModel.getSplashStatus().first()
        SplashAdLog.d("isFirstSession=$isFirstSession")

        val prepared = withTimeoutOrNull(waitLimitMs.milliseconds) {
            prepareSplashAd(ads, appFirebaseRemote, network)
        }
        if (prepared == null) SplashAdLog.d("prepare: TIMEOUT after ${waitLimitMs}ms")

        val elapsed = SystemClock.elapsedRealtime() - startedAt
        if (elapsed < SplashAdTiming.MIN_SPLASH_MS) {
            delay((SplashAdTiming.MIN_SPLASH_MS - elapsed).milliseconds)
        }

        if (isFirstSession) ads.preload(AdPlacement.LanguageScreenBottom)
        ads.preload(AdPlacement.HomeScreenBottom)
        if (isFirstSession && remoteConfigStore.current.showOnboardingScreen) {
            ads.preload(AdPlacement.OnboardScreenBottom)
        }

        creep.cancel()
        progress.animateTo(1f, tween(SplashAdTiming.PROGRESS_FINISH_MS))

        val showPremium = viewModel.showPremiumAfterSplash(isFirstSession)
        SplashAdLog.d("decision: showPremium=$showPremium")
        val finishSplash = {
            val nextStep =
                if (isFirstSession) StartPremiumNextStep.Language else StartPremiumNextStep.Home
            when {
                showPremium -> goToStartPremium(navParentController, nextStep)
                isFirstSession -> goToLanguage(navParentController)
                else -> goToHome(navParentController)
            }
        }

        val availableAtDecision = ads.isLoadedOnDemand(AdPlacement.SplashFullscreen)
        SplashAdLog.d("decision: availableAtDecision=$availableAtDecision")
        if (availableAtDecision) {
            ads.fullscreen(
                placement = AdPlacement.SplashFullscreen,
                continueWhenShown = true,
                onResult = { result ->
                    SplashAdLog.d("show result=$result wasShown=${result.wasShown}")
                    missedSplashAd.onSplashAdResult(ads, result.wasShown, isFirstSession)
                },
                onDone = finishSplash,
            )
        } else {
            SplashAdLog.d("MISSED: no splash ad available, navigating without ad")
            missedSplashAd.onSplashAdResult(ads, wasShown = false, isFirstSession = isFirstSession)
            finishSplash()
        }
    }

    SplashAmbientBackground {
        Image(
            painter = painterResource(R.drawable.pose_splash_logo),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 112.dp, height = 150.dp),
        )

        SplashProgressBar(
            progress = progress.value,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
        )

    }
}
