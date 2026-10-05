package com.aipose.camera.posematch.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
fun SystemBarsAppearanceEffect(isDarkTheme: Boolean) {
    val systemBars = LocalAppSystemBars.current ?: return
    DisposableEffect(systemBars, isDarkTheme) {
        systemBars.applyAppearance(isDarkTheme)
        onDispose { }
    }
}
