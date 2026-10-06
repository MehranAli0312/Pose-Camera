package com.aipose.camera.posematch.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aipose.camera.posematch.ads.AppOpenRouteGate
import com.aipose.camera.posematch.ads.InnerInterstitialAds
import com.aipose.camera.posematch.ui.common.getActivity
import com.aipose.camera.posematch.ui.graph.DashboardNavGraph
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.bottomBarRoutes
import com.aipose.camera.posematch.ui.graph.navigateToTabNow
import com.aipose.camera.posematch.ui.screens.bottomBar.DashboardBottomAdSpace
import com.aipose.camera.posematch.ui.screens.bottomBar.DashboardBottomBar
import com.aipose.camera.posematch.ui.screens.bottomBar.InScreenBottomBar
import com.aipose.camera.posematch.ui.screens.bottomBar.LocalInScreenBottomBar
import com.aipose.camera.posematch.ui.screens.bottomSheet.ExitPopUp
import org.koin.compose.koinInject

@Composable
fun DashboardScreen(
    navParentController: NavHostController,
    appOpenRouteGate: AppOpenRouteGate = koinInject(),
    innerInterstitialAds: InnerInterstitialAds = koinInject(),
) {
    val activity = getActivity()
    val navController = rememberNavController()
    LaunchedEffect(innerInterstitialAds) { innerInterstitialAds.startCapping() }
    LaunchedEffect(appOpenRouteGate) {
        navController.currentBackStackEntryFlow.collect { entry ->
            appOpenRouteGate.onRouteChanged(entry.destination.route)
        }
    }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val showExitDialog = rememberSaveable {
        mutableStateOf(false)
    }

    ExitPopUp(showExitDialog) {
        showExitDialog.value = false
        activity?.finishAffinity()
    }

    BackHandler {
        when {
            currentRoute == NavRoute.HomeScreenRoute.route -> {
                showExitDialog.value = true
            }

            currentRoute in bottomBarRoutes -> {
                navController.navigateToTabNow(NavRoute.HomeScreenRoute.route)
            }

            else -> {
                if (!navController.popBackStack()) {
                    showExitDialog.value = true
                }
            }
        }
    }

    val visibleEntries by navController.visibleEntries.collectAsState()
    val visibleRoutes = visibleEntries.map { it.destination.route }
    val isBottomBarRouteVisible = visibleRoutes.any { it in bottomBarRoutes }
    val shouldShowBottomBar = isBottomBarRouteVisible && visibleRoutes.all { it in bottomBarRoutes }

    var bottomAdHeight by remember { mutableStateOf(0.dp) }

    val bottomBar: @Composable (NavDestination?) -> Unit = { destination ->
        DashboardBottomBar(
            navController = navController,
            destination = destination,
            adSpace = DashboardBottomAdSpace(isLive = false, height = bottomAdHeight),
        )
    }

    val inScreenBottomBar = InScreenBottomBar(
        isVisible = isBottomBarRouteVisible && !shouldShowBottomBar,
        content = bottomBar,
    )

    CompositionLocalProvider(
        LocalInScreenBottomBar provides inScreenBottomBar,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                DashboardNavGraph(
                    navController = navController,
                )
            }

            if (shouldShowBottomBar) {
                DashboardBottomBar(
                    navController = navController,
                    destination = currentBackStackEntry?.destination,
                    applyNavigationBarInsets = true,
                    adSpace = DashboardBottomAdSpace(
                        isLive = true,
                        height = bottomAdHeight,
                        onHeightChanged = { height -> bottomAdHeight = height },
                    ),
                )
            }
        }
    }
}
