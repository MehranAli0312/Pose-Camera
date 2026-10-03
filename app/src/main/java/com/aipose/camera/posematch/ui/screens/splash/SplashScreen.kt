package com.aipose.camera.posematch.ui.screens.splash

import android.annotation.SuppressLint
import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.aipose.camera.posematch.ui.common.AppLogoImage
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.screens.splash.components.SplashAmbientBackground
import com.aipose.camera.posematch.ui.screens.splash.components.SplashTagline
import com.aipose.camera.posematch.ui.vm.SplashViewModel
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
    val colorScheme = MaterialTheme.colorScheme
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

    Box(modifier = Modifier.fillMaxSize()) {
        SplashAmbientBackground(modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val logoSize = 200.dp
            val logoToTextSpacing = 80.dp
            val logoVerticalOffset = (-100).dp

            AppLogoImage(
                size = logoSize,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = logoVerticalOffset)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = logoSize / 2 + logoToTextSpacing + logoVerticalOffset)
                    .padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                            )
                        ) {
                            append(stringResource(R.string.splash_brand_pose))
                        }
                        append(" ")
                        withStyle(
                            SpanStyle(
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        ) {
                            append(stringResource(R.string.splash_brand_match))
                        }
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 32.sp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                SplashTagline(mutedColor = colorScheme.onSurfaceVariant)
            }

            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(vertical = 40.dp, horizontal = 30.dp),
                color = colorScheme.primary,
                trackColor = colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
            )
        }
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
