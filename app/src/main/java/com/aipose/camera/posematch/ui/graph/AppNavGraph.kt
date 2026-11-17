package com.aipose.camera.posematch.ui.graph

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.navArgument
import com.aipose.camera.posematch.ads.rememberMissedSplashAd
import com.aipose.camera.posematch.ads.LaunchFlowAds
import com.aipose.camera.posematch.ui.common.addScreenWithTransitions
import com.aipose.camera.posematch.ui.screens.language.LocalizeScreen
import com.aipose.camera.posematch.ui.screens.onboard.OnboardScreen
import com.aipose.camera.posematch.ui.screens.pro.PremiumScreen
import com.aipose.camera.posematch.ui.screens.launch.navigateLaunchFlow
import com.aipose.camera.posematch.ui.screens.launch.models.LaunchFlow
import com.aipose.camera.posematch.ui.screens.launch.models.LaunchSession
import com.aipose.camera.posematch.ui.screens.launch.models.LaunchStep
import com.aipose.camera.posematch.ui.screens.splash.SplashScreen
import org.koin.compose.koinInject

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
        addMainScreens(navParentController)
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
    navGraphBuilder.addScreenWithTransitions(
        route = NavRoute.LanguageScreenRoute.launchRoute,
        arguments = listOf(navArgument(NavArgs.LAUNCH_FLOW) { type = NavType.StringType }),
    ) { backStackEntry ->
        val launchFlow = backStackEntry.launchFlow()
        LocalizeScreen(
            navController = navParentController,
            launchFlow = launchFlow,
            isFirstSession = launchFlow.session == LaunchSession.First,
        )
    }
}

private fun addOnboardScreen(
    navParentController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(
        route = NavRoute.OnboardScreenRoute.route,
        arguments = listOf(navArgument(NavArgs.LAUNCH_FLOW) { type = NavType.StringType }),
    ) { backStackEntry ->
        OnboardScreen(
            navController = navParentController,
            launchFlow = backStackEntry.launchFlow(),
        )
    }
}

private fun addStartPremiumScreen(
    navParentController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(
        route = NavRoute.StartPremiumScreenRoute.route,
        arguments = listOf(navArgument(NavArgs.LAUNCH_FLOW) { type = NavType.StringType }),
    ) { backStackEntry ->
        val launchFlow = backStackEntry.launchFlow()
        val missedSplashAd = rememberMissedSplashAd()
        val launchFlowAds: LaunchFlowAds = koinInject()
        PremiumScreen(
            navController = navParentController,
            onClose = {
                if (navParentController.acceptNavigationClick()) {
                    missedSplashAd.showThen {
                        navParentController.navigateLaunchFlow(launchFlow, launchFlowAds)
                    }
                }
            },
        )
    }
}

private fun androidx.navigation.NavBackStackEntry.launchFlow(): LaunchFlow =
    LaunchFlow.decode(Uri.decode(arguments?.getString(NavArgs.LAUNCH_FLOW))).orEmpty()

private fun LaunchFlow?.orEmpty(): LaunchFlow =
    this ?: LaunchFlow(LaunchSession.Returning, listOf(LaunchStep.Home))
