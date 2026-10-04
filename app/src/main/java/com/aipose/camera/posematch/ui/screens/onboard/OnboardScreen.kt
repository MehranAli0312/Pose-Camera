package com.aipose.camera.posematch.ui.screens.onboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseCtaButton
import com.aipose.camera.posematch.ui.screens.onboard.components.OnboardBackdrop
import com.aipose.camera.posematch.ui.screens.onboard.components.OnboardContentPage
import com.aipose.camera.posematch.ui.screens.onboard.components.OnboardPagerIndicator
import com.aipose.camera.posematch.ui.screens.onboard.components.OnboardTopBar
import com.aipose.camera.posematch.ui.screens.onboard.data.onboardSteps
import com.aipose.camera.posematch.ui.screens.splash.goToHome
import com.aipose.camera.posematch.ui.vm.SplashViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private val ScreenPadding = 20.dp
private val TopBarTop = 40.dp
private val IndicatorTop = 44.dp
private val IndicatorToCta = 30.dp
private val CtaBottom = 36.dp

@Composable
fun OnboardScreen(
    navController: NavHostController,
    splashViewModel: SplashViewModel = koinViewModel(),
) {
    val steps = remember { onboardSteps }
    val pagerState = rememberPagerState(pageCount = { steps.size })
    val scope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val currentStep = steps[currentPage]
    val isLastPage = currentPage == steps.lastIndex

    fun finishOnboarding() {
        scope.launch {
            splashViewModel.writeSplashStatus()
            goToHome(navController)
        }
    }

    fun goToPage(page: Int) {
        scope.launch { pagerState.animateScrollToPage(page) }
    }

    BackHandler(enabled = currentPage > 0) {
        goToPage(currentPage - 1)
    }

    OnboardBackdrop(
        accent = currentStep.ambientAccent,
        accentAlpha = currentStep.ambientAccentAlpha,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(TopBarTop))
            OnboardTopBar(
                showBack = currentPage > 0,
                showSkip = !isLastPage,
                onBack = { goToPage(currentPage - 1) },
                onSkip = ::finishOnboarding,
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { page ->
                OnboardContentPage(
                    step = steps[page],
                    stepNumber = page + 1,
                    totalSteps = steps.size,
                )
            }
            Spacer(modifier = Modifier.height(IndicatorTop))
            OnboardPagerIndicator(
                totalPages = steps.size,
                currentPage = currentPage,
            )
            Spacer(modifier = Modifier.height(IndicatorToCta))
            PoseCtaButton(
                text = stringResource(currentStep.ctaRes),
                onClick = {
                    if (isLastPage) finishOnboarding() else goToPage(currentPage + 1)
                },
                trailingIconRes = R.drawable.ic_pose_chevron_cta,
            )
            Spacer(modifier = Modifier.height(CtaBottom))
        }
    }
}
