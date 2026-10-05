package com.aipose.camera.posematch.ui.screens.onboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.ads.rememberMissedSplashAd
import com.aipose.camera.posematch.ads.rememberOnboardingNativeAd
import com.aipose.camera.posematch.ui.screens.onboard.components.OnboardBackdrop
import com.aipose.camera.posematch.ui.screens.onboard.components.OnboardBottomAd
import com.aipose.camera.posematch.ui.screens.onboard.components.OnboardChrome
import com.aipose.camera.posematch.ui.screens.onboard.components.OnboardNativeAdPage
import com.aipose.camera.posematch.ui.screens.onboard.components.OnboardStepPage
import com.aipose.camera.posematch.ui.screens.onboard.data.onboardSteps
import com.aipose.camera.posematch.ui.screens.onboard.models.OnboardPage
import com.aipose.camera.posematch.ui.screens.onboard.models.onboardPages
import com.aipose.camera.posematch.ui.screens.onboard.models.stepIndexAt
import com.aipose.camera.posematch.ui.screens.splash.goToHome
import com.aipose.camera.posematch.ui.vm.SplashViewModel
import kotlin.math.abs
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardScreen(
    navController: NavHostController,
    splashViewModel: SplashViewModel = koinViewModel(),
) {
    val steps = remember { onboardSteps }
    val nativeAd = rememberOnboardingNativeAd()
    val adPageIndex = nativeAd.pageIndex
    val pages = remember(steps, adPageIndex) { onboardPages(steps, adPageIndex) }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val missedSplashAd = rememberMissedSplashAd()
    val currentPage = pagerState.currentPage
    val currentStepIndex = pages.stepIndexAt(currentPage)
    val currentStep = steps[currentStepIndex]
    val isLastPage = currentPage == pages.lastIndex
    val isChromeVisible by remember(pagerState, adPageIndex) {
        derivedStateOf { pagerState.chromeAlpha(adPageIndex) > 0f }
    }
    val isBottomAdVisible by remember(pagerState, adPageIndex) {
        derivedStateOf { pagerState.settledPage != adPageIndex }
    }
    var bottomAdHeight by remember { mutableStateOf(0.dp) }
    val adPageBottomReserved by animateDpAsState(
        targetValue = if (isBottomAdVisible) bottomAdHeight else 0.dp,
        label = AD_PAGE_BOTTOM_LABEL,
    )

    LaunchedEffect(nativeAd, pagerState) {
        nativeAd.insertWhenReady { candidate ->
            pagerState.currentPage < candidate && !pagerState.isScrollInProgress
        }
    }

    fun finishOnboarding() {
        missedSplashAd.showThen {
            missedSplashAd.clear()
            scope.launch {
                splashViewModel.writeSplashStatus()
                goToHome(navController)
            }
        }
    }

    fun goToPage(page: Int) {
        scope.launch { pagerState.animateScrollToPage(page) }
    }

    fun goToNextPage() {
        missedSplashAd.showThen { goToPage(currentPage + 1) }
    }

    BackHandler(enabled = true) {}

    OnboardBackdrop(
        accent = currentStep.ambientAccent,
        accentAlpha = currentStep.ambientAccentAlpha,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { page -> pages[page].key },
        ) { page ->
            when (val onboardPage = pages[page]) {
                is OnboardPage.Step -> OnboardStepPage(
                    step = onboardPage.step,
                    bottomReserved = bottomAdHeight,
                )
                OnboardPage.NativeAd -> OnboardNativeAdPage(
                    onContinue = { goToPage(page + 1) },
                    bottomReserved = adPageBottomReserved,
                )
            }
        }
        if (isChromeVisible) {
            OnboardChrome(
                showSkip = !isLastPage,
                totalSteps = steps.size,
                currentStep = currentStepIndex,
                ctaText = stringResource(currentStep.ctaRes),
                onSkip = ::finishOnboarding,
                onCta = { if (isLastPage) finishOnboarding() else goToNextPage() },
                modifier = Modifier.graphicsLayer { alpha = pagerState.chromeAlpha(adPageIndex) },
                bottomReserved = bottomAdHeight,
            )
        }
        if (isBottomAdVisible) {
            OnboardBottomAd(
                onHeightChanged = { height -> bottomAdHeight = height },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

private const val AD_PAGE_BOTTOM_LABEL = "onboardAdPageBottom"

private fun PagerState.chromeAlpha(adPageIndex: Int?): Float {
    if (adPageIndex == null) return 1f
    val position = currentPage + currentPageOffsetFraction
    return abs(position - adPageIndex).coerceIn(0f, 1f)
}
