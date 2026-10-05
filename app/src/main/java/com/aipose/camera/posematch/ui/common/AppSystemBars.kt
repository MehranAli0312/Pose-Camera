package com.aipose.camera.posematch.ui.common

import android.view.Window
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class AppSystemBars(private val window: Window) {

    private val controller: WindowInsetsControllerCompat by lazy {
        WindowCompat.getInsetsController(window, window.decorView)
    }

    private var appliedLightBars: Boolean? = null

    fun applyImmersiveBehavior() {
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    fun applyAppearance(isDarkTheme: Boolean) {
        val lightBars = !isDarkTheme
        if (appliedLightBars == lightBars) return
        appliedLightBars = lightBars
        controller.isAppearanceLightStatusBars = lightBars
        controller.isAppearanceLightNavigationBars = lightBars
    }
}

val LocalAppSystemBars = staticCompositionLocalOf<AppSystemBars?> { null }
