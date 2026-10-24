package com.aipose.camera.posematch.ui.graph

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.navArgument
import com.aipose.camera.posematch.ui.common.addScreenWithTransitions
import com.aipose.camera.posematch.ui.screens.DashboardScreen
import com.aipose.camera.posematch.ui.screens.language.LocalizeScreen
import com.aipose.camera.posematch.ui.screens.onboard.OnboardScreen
import com.aipose.camera.posematch.ui.screens.pro.PremiumScreen
import com.aipose.camera.posematch.ui.screens.pro.models.StartPremiumNextStep
import com.aipose.camera.posematch.ui.screens.splash.SplashScreen
import com.aipose.camera.posematch.ui.screens.splash.continueAfterStartPremium

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
        addStartPremiumScreen(navParentController, this)
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

private fun addStartPremiumScreen(
    navParentController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(
        route = NavRoute.StartPremiumScreenRoute.route,
        arguments = listOf(navArgument(NavArgs.NEXT_STEP) { type = NavType.StringType }),
    ) { backStackEntry ->
        val nextStep = StartPremiumNextStep.entries.firstOrNull {
            it.name == backStackEntry.arguments?.getString(NavArgs.NEXT_STEP)
        } ?: StartPremiumNextStep.Home
        PremiumScreen(
            navController = navParentController,
            onClose = {
                if (navParentController.acceptNavigationClick()) {
                    continueAfterStartPremium(navParentController, nextStep)
                }
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
