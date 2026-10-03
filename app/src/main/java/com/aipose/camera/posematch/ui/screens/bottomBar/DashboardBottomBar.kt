package com.aipose.camera.posematch.ui.screens.bottomBar

import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.aipose.camera.posematch.ui.theme.GrayGeneric

@Composable
fun DashboardBottomBar(
    navController: NavController,
    destination: NavDestination?,
    applyNavigationBarInsets: Boolean = true,
) {
    val windowInsets = if (applyNavigationBarInsets) {
        BottomAppBarDefaults.windowInsets
    } else {
        BottomAppBarDefaults.windowInsets.only(WindowInsetsSides.Horizontal)
    }

    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 12.dp)
            .shadow(
                elevation = 15.dp,
                shape = RoundedCornerShape(10.dp),
                clip = false,
                spotColor = GrayGeneric,
            )
            .clip(RoundedCornerShape(10.dp)),
        containerColor = MaterialTheme.colorScheme.surface,
        windowInsets = windowInsets,
    ) {
        BottomNavigationBar(
            navController = navController,
            selectedDestination = destination,
        )
    }
}
