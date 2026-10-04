package com.aipose.camera.posematch.ui.screens.bottomBar

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination

@Composable
fun DashboardBottomBar(
    navController: NavController,
    destination: NavDestination?,
    applyNavigationBarInsets: Boolean = true,
) {
    BottomNavigationBar(
        navController = navController,
        selectedDestination = destination,
        modifier = if (applyNavigationBarInsets) {
            Modifier.navigationBarsPadding()
        } else {
            Modifier
        },
    )
}
