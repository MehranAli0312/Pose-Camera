package com.aipose.camera.posematch.ads

import com.aipose.camera.posematch.ui.graph.NavRoute
import java.util.concurrent.atomic.AtomicReference

class AppOpenRouteGate {

    private val currentRoute = AtomicReference<String?>(null)

    fun onRouteChanged(route: String?) {
        currentRoute.set(route)
    }

    fun allowsAppOpen(): Boolean = currentRoute.get() !in CaptureFlowRoutes

    private companion object {
        val CaptureFlowRoutes = setOf(
            NavRoute.CameraScreenRoute.route,
            NavRoute.PhotoEditScreenRoute.route,
        )
    }
}
