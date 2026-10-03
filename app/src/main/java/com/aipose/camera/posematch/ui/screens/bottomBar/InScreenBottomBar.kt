package com.aipose.camera.posematch.ui.screens.bottomBar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination

@Immutable
data class InScreenBottomBar(
    val isVisible: Boolean,
    val content: @Composable (NavDestination?) -> Unit,
)

val LocalInScreenBottomBar = compositionLocalOf {
    InScreenBottomBar(isVisible = false, content = {})
}

@Composable
fun InScreenBottomBarHost(
    destination: NavDestination,
    content: @Composable () -> Unit,
) {
    val bottomBar = LocalInScreenBottomBar.current
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
        if (bottomBar.isVisible) {
            bottomBar.content(destination)
        }
    }
}
