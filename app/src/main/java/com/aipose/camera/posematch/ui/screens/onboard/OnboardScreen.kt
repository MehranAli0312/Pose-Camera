package com.aipose.camera.posematch.ui.screens.onboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.screens.onboard.components.OnboardBackdrop
import com.aipose.camera.posematch.ui.screens.onboard.components.OnboardBottomAction
import com.aipose.camera.posematch.ui.screens.onboard.components.OnboardContentPage
import com.aipose.camera.posematch.ui.screens.onboard.components.OnboardPagerIndicator
import com.aipose.camera.posematch.ui.screens.onboard.components.OnboardSkipPill
import com.aipose.camera.posematch.ui.screens.onboard.components.OnboardSlideText
import com.aipose.camera.posematch.ui.screens.onboard.data.onboardSlides
import com.aipose.camera.posematch.ui.screens.splash.goToHome
import com.aipose.camera.posematch.ui.vm.SplashViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardScreen(
    navController: NavHostController,
    splashViewModel: SplashViewModel = koinViewModel(),
) {
    val pages = remember { onboardSlides }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val isLastPage = currentPage == pages.lastIndex

    fun finishOnboarding() {
        scope.launch {
            splashViewModel.writeSplashStatus()
            goToHome(navController)
        }
    }

    BackHandler {
        if (currentPage > 0) {
            scope.launch { pagerState.animateScrollToPage(currentPage - 1) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        OnboardBackdrop(modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OnboardSkipPill(onSkip = ::finishOnboarding)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { page ->
                OnboardContentPage(slide = pages[page])
            }
            Spacer(modifier = Modifier.height(20.dp))
            OnboardPagerIndicator(
                totalPages = pages.size,
                currentPage = currentPage,
            )
            Spacer(modifier = Modifier.height(24.dp))
            OnboardSlideText(slide = pages.getOrNull(currentPage))
            Spacer(modifier = Modifier.height(28.dp))
            Box(modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 20.dp)) {
                OnboardBottomAction(
                    isLastPage = isLastPage,
                    buttonText = if (isLastPage) {
                        stringResource(R.string.get_started)
                    } else {
                        stringResource(R.string.onboard_continue)
                    },
                    onFinish = ::finishOnboarding,
                    onNext = {
                        scope.launch {
                            pagerState.animateScrollToPage(currentPage + 1)
                        }
                    },
                )
            }
        }
    }
}
