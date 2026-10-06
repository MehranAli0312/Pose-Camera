package com.aipose.camera.posematch.ui.screens.bottomBar

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.aipose.camera.posematch.ui.common.safeBottomSystemBarsPadding
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.aipose.camera.posematch.ui.firebaseRemote.BottomAdPosition
import org.koin.compose.koinInject

@Composable
internal fun DashboardBottomBar(
    navController: NavController,
    destination: NavDestination?,
    applyNavigationBarInsets: Boolean = true,
    adSpace: DashboardBottomAdSpace = DashboardBottomAdSpace(isLive = false, height = 0.dp),
    remoteConfigStore: AdsRemoteConfigStore = koinInject(),
) {
    val remoteConfig by remoteConfigStore.config.collectAsState()
    val adPosition = remoteConfig.homeScreenBottomAd

    Column(
        modifier = if (applyNavigationBarInsets) {
            Modifier.safeBottomSystemBarsPadding()
        } else {
            Modifier
        },
    ) {
        if (adPosition == BottomAdPosition.AboveBottomBar) DashboardBottomAd(space = adSpace)
        BottomNavigationBar(
            navController = navController,
            selectedDestination = destination,
        )
        if (adPosition == BottomAdPosition.BelowBottomBar) DashboardBottomAd(space = adSpace)
    }
}
