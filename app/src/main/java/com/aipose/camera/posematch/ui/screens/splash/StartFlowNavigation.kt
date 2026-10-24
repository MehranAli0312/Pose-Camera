package com.aipose.camera.posematch.ui.screens.splash

import androidx.navigation.NavHostController
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.screens.pro.models.StartPremiumNextStep
import com.example.common.Constants.splashEnd

fun goToLanguage(navController: NavHostController) {
    splashEnd = true
    navController.navigate(NavRoute.LanguageScreenRoute.route) {
        popUpTo(NavRoute.SplashScreenRoute.route) { inclusive = true }
    }
}

fun goToHome(navController: NavHostController) {
    splashEnd = true
    navController.navigateAsStartRoot(NavRoute.DashboardScreenRoute.route)
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

fun goToStartPremium(navController: NavHostController, nextStep: StartPremiumNextStep) {
    splashEnd = true
    navController.navigateAsStartRoot(NavRoute.StartPremiumScreenRoute.routeFor(nextStep))
}

fun continueAfterStartPremium(navController: NavHostController, nextStep: StartPremiumNextStep) {
    when (nextStep) {
        StartPremiumNextStep.Language ->
            navController.navigateAsStartRoot(NavRoute.LanguageScreenRoute.route)
        StartPremiumNextStep.Onboard ->
            navController.navigateAsStartRoot(NavRoute.OnboardScreenRoute.route)
        StartPremiumNextStep.Home -> goToHome(navController)
    }
}

private fun NavHostController.navigateAsStartRoot(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
