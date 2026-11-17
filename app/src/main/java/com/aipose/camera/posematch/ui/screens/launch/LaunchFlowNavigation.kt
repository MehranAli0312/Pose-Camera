package com.aipose.camera.posematch.ui.screens.launch

import androidx.navigation.NavHostController
import com.aipose.camera.posematch.ads.LaunchFlowAds
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.screens.launch.models.LaunchFlow
import com.aipose.camera.posematch.ui.screens.launch.models.LaunchStep
import com.example.common.Constants.splashEnd

fun NavHostController.navigateLaunchFlow(
    launchFlow: LaunchFlow,
    launchFlowAds: LaunchFlowAds,
) {
    val nextStep = launchFlow.steps.firstOrNull() ?: LaunchStep.Home
    val remaining = launchFlow.remaining()
    launchFlowAds.preloadFor(nextStep)
    splashEnd = true
    val destination = when (nextStep) {
        LaunchStep.Premium -> NavRoute.StartPremiumScreenRoute.routeFor(remaining)
        LaunchStep.Language -> NavRoute.LanguageScreenRoute.routeFor(remaining)
        LaunchStep.Onboarding -> NavRoute.OnboardScreenRoute.routeFor(remaining)
        LaunchStep.Home -> NavRoute.HomeScreenRoute.route
    }
    navigate(destination) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
