package com.aipose.camera.posematch.ui.graph

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.aipose.camera.posematch.ads.rememberMissedSplashAd
import com.aipose.camera.posematch.ui.common.addScreenWithTransitions
import com.aipose.camera.posematch.ui.screens.DashboardScreen
import com.aipose.camera.posematch.ui.screens.language.LocalizeScreen
import com.aipose.camera.posematch.ui.screens.onboard.OnboardScreen
import com.aipose.camera.posematch.ui.screens.pro.ProScreen
import com.aipose.camera.posematch.ui.screens.splash.SplashScreen
import com.aipose.camera.posematch.ui.screens.splash.goToHome

@Composable
fun AppNavGraph(
    navParentController: NavHostController,
) {
    NavHost(
        modifier = Modifier,
        navController = navParentController,
        startDestination = NavRoute.SplashScreenRoute.route
    ) {
        addSplashScreen(navParentController, this)
        addFirstSessionLanguageScreen(navParentController, this)
        addOnboardScreen(navParentController, this)
        addSplashProScreen(navParentController, this)
        addDashboardScreen(navParentController, this)
    }
}

private fun addSplashScreen(
    navParentController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(route = NavRoute.SplashScreenRoute.route) {
        SplashScreen(navParentController = navParentController)
    }
}

private fun addFirstSessionLanguageScreen(
    navParentController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(route = NavRoute.LanguageScreenRoute.route) {
        LocalizeScreen(
            navController = navParentController,
            isFirstSession = true,
        )
    }
}

private fun addOnboardScreen(
    navParentController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(route = NavRoute.OnboardScreenRoute.route) {
        OnboardScreen(navController = navParentController)
    }
}

private fun addSplashProScreen(
    navParentController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(route = NavRoute.SplashProScreenRoute.route) {
        val missedSplashAd = rememberMissedSplashAd()
        ProScreen(
            navController = navParentController,
            onClose = { missedSplashAd.showThen { goToHome(navParentController) } },
            onPurchased = {
                missedSplashAd.clear()
                goToHome(navParentController)
            },
        )
    }
}

private fun addDashboardScreen(
    navParentController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(
        route = NavRoute.DashboardScreenRoute.route
    ) {
        DashboardScreen(navParentController)
    }
}
