package com.aipose.camera.posematch.ui.screens.bottomBar

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.aipose.camera.posematch.ads.HomeScreenBottom
import com.aipose.camera.posematch.ui.common.safeBottomSystemBarsPadding
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.aipose.camera.posematch.ui.firebaseRemote.BottomAdPosition
import com.example.ads.AdPlacement
import com.example.ads.compose.AdsSlot
import org.koin.compose.koinInject

@Composable
fun DashboardBottomBar(
    navController: NavController,
    destination: NavDestination?,
    applyNavigationBarInsets: Boolean = true,
    adsRemoteConfigStore: AdsRemoteConfigStore = koinInject(),
) {
    val adsConfig by adsRemoteConfigStore.config.collectAsState()
    val bottomInsets = if (applyNavigationBarInsets) {
        Modifier.safeBottomSystemBarsPadding()
    } else {
        Modifier
    }

    when (adsConfig.positionFor(AdPlacement.HomeScreenBottom)) {
        BottomAdPosition.Off -> BottomNavigationBar(
            navController = navController,
            selectedDestination = destination,
            modifier = bottomInsets,
        )

        BottomAdPosition.AboveBottomBar -> Column {
            DashboardBottomAd()
            BottomNavigationBar(
                navController = navController,
                selectedDestination = destination,
                modifier = bottomInsets,
            )
        }

        BottomAdPosition.BelowBottomBar -> Column {
            BottomNavigationBar(
                navController = navController,
                selectedDestination = destination,
            )
            DashboardBottomAd(modifier = bottomInsets)
        }
    }
}

@Composable
private fun DashboardBottomAd(modifier: Modifier = Modifier) {
    AdsSlot(
        placement = AdPlacement.HomeScreenBottom,
        modifier = modifier,
    )
}
