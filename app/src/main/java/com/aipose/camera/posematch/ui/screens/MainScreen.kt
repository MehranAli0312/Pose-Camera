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
import androidx.navigation.compose.rememberNavController
import com.aipose.camera.posematch.ads.AppOpenLoaderState
import com.aipose.camera.posematch.ads.AppOpenRouteGate
import com.aipose.camera.posematch.ui.common.SystemBarsAppearanceEffect
import com.aipose.camera.posematch.ui.graph.AppNavGraph
import com.aipose.camera.posematch.ui.screens.components.AppOpenLoadingOverlay
import com.aipose.camera.posematch.ui.theme.MyAppTheme
import com.aipose.camera.posematch.ui.vm.LanguageViewModel
import org.koin.compose.koinInject

@Composable
fun MainScreen(
    languageChangeViewModel: LanguageViewModel = koinInject(),
    appOpenLoaderState: AppOpenLoaderState = koinInject(),
    appOpenRouteGate: AppOpenRouteGate = koinInject(),
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
            LaunchedEffect(appOpenRouteGate) {
                navParentController.currentBackStackEntryFlow.collect { entry ->
                    appOpenRouteGate.onRouteChanged(entry.destination.route)
                }
            }
            val isAppOpenLoading by appOpenLoaderState.isVisible.collectAsStateWithLifecycle()
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                AppNavGraph(
                    navParentController = navParentController
                )
                if (isAppOpenLoading) AppOpenLoadingOverlay()
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
