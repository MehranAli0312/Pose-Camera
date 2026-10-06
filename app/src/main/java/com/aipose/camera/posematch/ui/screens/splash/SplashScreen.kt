package com.aipose.camera.posematch.ui.screens.splash

import android.annotation.SuppressLint
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ads.AdPlacement
import com.example.common.Constants.splashEnd
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ads.LanguageScreenBottom
import com.aipose.camera.posematch.ads.MissedSplashAd
import com.aipose.camera.posematch.ads.OnboardScreenBottom
import com.aipose.camera.posematch.ads.prepareSplashAd
import com.aipose.camera.posematch.ads.SplashAdTiming
import com.aipose.camera.posematch.ads.SplashFullscreen
import com.aipose.camera.posematch.ads.rememberScreenAds
import com.aipose.camera.posematch.data.local.NetworkConnectivityChecker
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.screens.splash.components.SplashAmbientBackground
import com.aipose.camera.posematch.ui.screens.splash.components.SplashBrandMark
import com.aipose.camera.posematch.ui.screens.splash.components.SplashProgressBar
import com.aipose.camera.posematch.ui.screens.splash.components.SplashTagline
import com.aipose.camera.posematch.ui.screens.splash.components.SplashWordmark
import com.aipose.camera.posematch.ui.vm.SplashViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

private val WatermarkWidth = 200.dp
private val WatermarkHeight = 330.dp
private val WatermarkOffsetY = (-87).dp
private val ContentOffsetY = (-22).dp
private val BrandMarkToWordmark = 20.dp
private val WordmarkToTagline = 13.dp
private val TaglineToProgress = 48.dp

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

        ads.keepWarm(AdPlacement.SplashFullscreen)

        val creep = launch {
            progress.animateTo(
                targetValue = SplashAdTiming.PROGRESS_CREEP_TARGET,
                animationSpec = tween(
                    durationMillis = maxWaitMs.toInt(),
                    easing = LinearEasing,
                ),
            )
        }

        val isFirstSession = viewModel.getSplashStatus().first()

        withTimeoutOrNull(maxWaitMs.milliseconds) {
            prepareSplashAd(ads, appFirebaseRemote, network)
        }

        val elapsed = SystemClock.elapsedRealtime() - startedAt
        if (elapsed < SplashAdTiming.MIN_SPLASH_MS) {
            delay((SplashAdTiming.MIN_SPLASH_MS - elapsed).milliseconds)
        }

        if (isFirstSession) ads.preload(AdPlacement.LanguageScreenBottom)
        if (isFirstSession && remoteConfigStore.current.showOnboardingScreen) {
            ads.preload(AdPlacement.OnboardScreenBottom)
        }

        creep.cancel()
        progress.animateTo(1f, tween(SplashAdTiming.PROGRESS_FINISH_MS))

        val finishSplash = {
            if (isFirstSession) goToLanguage(navParentController) else goToHome(navParentController)
        }

        if (ads.isAvailable(AdPlacement.SplashFullscreen)) {
            ads.fullscreen(
                placement = AdPlacement.SplashFullscreen,
                continueWhenShown = true,
                preloadedOnly = true,
                onResult = { result ->
                    missedSplashAd.onSplashAdResult(ads, result.wasShown, isFirstSession)
                },
                onDone = finishSplash,
            )
        } else {
            missedSplashAd.onSplashAdResult(ads, wasShown = false, isFirstSession = isFirstSession)
            finishSplash()
        }
    }

    SplashAmbientBackground {
        Image(
            painter = painterResource(R.drawable.ic_splash_pose_watermark),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = WatermarkOffsetY)
                .size(width = WatermarkWidth, height = WatermarkHeight),
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = ContentOffsetY),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SplashBrandMark()
            Spacer(modifier = Modifier.height(BrandMarkToWordmark))
            SplashWordmark()
            Spacer(modifier = Modifier.height(WordmarkToTagline))
            SplashTagline()
            Spacer(modifier = Modifier.height(TaglineToProgress))
            SplashProgressBar(progress = progress.value)
        }
    }
}

fun goToLanguage(navController: NavHostController) {
    splashEnd = true
    navController.navigate(NavRoute.LanguageScreenRoute.route) {
        popUpTo(NavRoute.SplashScreenRoute.route) { inclusive = true }
    }
}

fun goToHome(navController: NavHostController) {
    splashEnd = true
    navController.navigate(NavRoute.DashboardScreenRoute.route) {
        popUpTo(navController.graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

fun goToOnboardOrNext(
    navController: NavHostController,
    showOnboarding: Boolean,
) {
    if (showOnboarding) goToOnboard(navController) else goToHome(navController)
}

fun goToOnboard(navController: NavHostController) {
    navController.navigate(NavRoute.OnboardScreenRoute.route) {
        popUpTo(NavRoute.LanguageScreenRoute.route) { inclusive = true }
        launchSingleTop = true
    }
}
