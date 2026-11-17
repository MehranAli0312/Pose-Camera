package com.aipose.camera.posematch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aipose.camera.posematch.ads.AdLoadingCoverState
import com.aipose.camera.posematch.ads.AppOpenRouteGate
import com.aipose.camera.posematch.ads.InnerInterstitialAds
import com.aipose.camera.posematch.data.notification.NotificationLaunchStore
import com.aipose.camera.posematch.ui.common.RequestNotificationPermissionOnce
import com.aipose.camera.posematch.ui.common.SystemBarsAppearanceEffect
import com.aipose.camera.posematch.ui.graph.AppNavGraph
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.route
import com.aipose.camera.posematch.ui.screens.components.AdLoadingOverlay
import com.aipose.camera.posematch.ui.theme.MyAppTheme
import com.aipose.camera.posematch.ui.vm.LanguageViewModel
import com.example.ads.AdsManager
import com.example.common.Constants.splashEnd
import org.koin.compose.koinInject

@Composable
fun MainScreen(
    languageChangeViewModel: LanguageViewModel = koinInject(),
    adLoadingCoverState: AdLoadingCoverState = koinInject(),
    appOpenRouteGate: AppOpenRouteGate = koinInject(),
    innerInterstitialAds: InnerInterstitialAds = koinInject(),
    notificationLaunchStore: NotificationLaunchStore = koinInject(),
    adsManager: AdsManager = koinInject(),
) {
    SystemBarsAppearanceEffect(isDarkTheme = true)

    MyAppTheme {

        val currentLanguageCode = languageChangeViewModel.currentLanguageCode
        val isLanguageLoaded = languageChangeViewModel.isLanguageLoaded

        if (isLanguageLoaded) {
            remember(currentLanguageCode) {
                if (currentLanguageCode.isNotBlank()) {
                    languageChangeViewModel.applyLanguage(currentLanguageCode)
                }
                true
            }

            val navParentController = rememberNavController()
            val currentBackStackEntry by navParentController.currentBackStackEntryAsState()
            val pendingNotificationTarget by notificationLaunchStore.pending.collectAsStateWithLifecycle()
            val isFullScreenAdVisible by adsManager.isFullScreenAdVisible.collectAsStateWithLifecycle()

            LaunchedEffect(innerInterstitialAds) { innerInterstitialAds.startCapping() }
            LaunchedEffect(appOpenRouteGate) {
                navParentController.currentBackStackEntryFlow.collect { entry ->
                    appOpenRouteGate.onRouteChanged(entry.destination.route)
                }
            }
            LaunchedEffect(pendingNotificationTarget, currentBackStackEntry?.destination?.route) {
                if (!splashEnd) return@LaunchedEffect
                val target = notificationLaunchStore.consume() ?: return@LaunchedEffect
                navParentController.navigate(target.route()) { launchSingleTop = true }
            }
            RequestNotificationPermissionOnce(
                canRequest = currentBackStackEntry?.destination?.route ==
                    NavRoute.HomeScreenRoute.route && !isFullScreenAdVisible,
            )
            val adLoadingCover by adLoadingCoverState.cover.collectAsStateWithLifecycle()
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                AppNavGraph(
                    navParentController = navParentController
                )
                adLoadingCover?.let { cover -> AdLoadingOverlay(cover) }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.background
                    )
            )
        }
    }
}
