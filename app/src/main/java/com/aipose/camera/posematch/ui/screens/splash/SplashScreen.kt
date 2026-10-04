package com.aipose.camera.posematch.ui.screens.splash

import android.annotation.SuppressLint
import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import com.example.ads.AdPlacement
import com.example.ads.AdsManager
import com.example.common.Constants.splashEnd
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ads.MissedSplashAd
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
import com.aipose.camera.posematch.ui.screens.splash.components.SplashPrivacyPill
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
private val PillBottomPadding = 24.dp

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
    val adsManager: AdsManager = koinInject()
    val missedSplashAd: MissedSplashAd = koinInject()

    LaunchedEffect(Unit) {
        val startedAt = SystemClock.elapsedRealtime()

        val creep = launch {
            progress.animateTo(
                targetValue = SplashAdTiming.PROGRESS_CREEP_TARGET,
                animationSpec = tween(
                    durationMillis = SplashAdTiming.MAX_TOTAL_WAIT_MS.toInt(),
                    easing = LinearEasing,
                ),
            )
        }

        val isFirstSession = viewModel.getSplashStatus().first()

        withTimeoutOrNull(SplashAdTiming.MAX_TOTAL_WAIT_MS.milliseconds) {
            prepareSplashAd(ads, appFirebaseRemote, network)
        }

        val elapsed = SystemClock.elapsedRealtime() - startedAt
        if (elapsed < SplashAdTiming.MIN_SPLASH_MS) {
            delay((SplashAdTiming.MIN_SPLASH_MS - elapsed).milliseconds)
        }

        creep.cancel()
        progress.animateTo(1f, tween(SplashAdTiming.PROGRESS_FINISH_MS))

        ads.fullscreen(
            placement = AdPlacement.SplashFullscreen,
            continueWhenShown = true,
            onResult = missedSplashAd::onSplashAdResult,
        ) {
            when {
                isFirstSession -> goToLanguage(navParentController)
                remoteConfigStore.current.splashToPremium && !adsManager.isPro.value ->
                    goToSplashPremium(navParentController)
                else -> goToHome(navParentController)
            }
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

        SplashPrivacyPill(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = PillBottomPadding),
        )
    }
}

fun goToLanguage(navController: NavHostController) {
    splashEnd = true
    navController.navigate(NavRoute.LanguageScreenRoute.route) {
        popUpTo(NavRoute.SplashScreenRoute.route) { inclusive = true }
    }
}

fun goToSplashPremium(navController: NavHostController) {
    splashEnd = true
    navController.navigate(NavRoute.SplashProScreenRoute.route) {
        popUpTo(NavRoute.SplashScreenRoute.route) { inclusive = true }
        launchSingleTop = true
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
