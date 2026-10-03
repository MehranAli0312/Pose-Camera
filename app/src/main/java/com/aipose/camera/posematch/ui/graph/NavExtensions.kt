package com.aipose.camera.posematch.ui.graph

import android.os.SystemClock
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

private const val NAVIGATION_CLICK_INTERVAL_MS = 500L
private var lastNavigationClickAtMs = 0L

fun NavController.acceptNavigationClick(): Boolean {
    if (currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return false
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
