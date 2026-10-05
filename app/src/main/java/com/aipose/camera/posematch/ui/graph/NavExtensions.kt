package com.aipose.camera.posematch.ui.graph

import android.os.SystemClock
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptionsBuilder

private const val NAVIGATION_CLICK_INTERVAL_MS = 400L
private var lastNavigationClickAtMs = 0L

fun NavController.acceptNavigationClick(): Boolean {
    val state = currentBackStackEntry?.lifecycle?.currentState ?: return false
    if (!state.isAtLeast(Lifecycle.State.STARTED)) return false
    val now = SystemClock.elapsedRealtime()
    if (now - lastNavigationClickAtMs < NAVIGATION_CLICK_INTERVAL_MS) return false
    lastNavigationClickAtMs = now
    return true
}

fun NavController.navigateOnClick(route: String, builder: NavOptionsBuilder.() -> Unit = {}) {
    if (acceptNavigationClick()) navigate(route, builder)
}

fun NavController.popBackStackOnClick() {
    if (acceptNavigationClick()) popBackStack()
}

fun NavController.navigateToTab(route: String) {
    if (!acceptNavigationClick()) return
    navigateToTabNow(route)
}

fun NavController.navigateToTabNow(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id)
        launchSingleTop = true
    }
}
