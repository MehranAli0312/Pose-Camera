package com.aipose.camera.posematch.ui.common

import android.app.Activity
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.aipose.camera.posematch.ui.theme.SurfaceBar
import com.aipose.camera.posematch.ui.theme.SurfaceBarDark

private val AppImmersiveBarTypes: Int =
    WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()

fun Window.applyAppImmersiveSystemBars() {
    val controller = WindowCompat.getInsetsController(this, decorView)
    controller.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    controller.hide(AppImmersiveBarTypes)
}

@Composable
fun AppSystemBars(isDarkTheme: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return

    val activity = LocalContext.current as? ComponentActivity ?: return
    val barColor = (if (isDarkTheme) SurfaceBarDark else SurfaceBar).toArgb()
    val darkScrim = SurfaceBarDark.toArgb()

    SideEffect {
        val statusBarStyle = if (isDarkTheme) {
            SystemBarStyle.dark(barColor)
        } else {
            SystemBarStyle.light(barColor, darkScrim)
        }
        val navigationBarStyle = if (isDarkTheme) {
            SystemBarStyle.dark(barColor)
        } else {
            SystemBarStyle.light(barColor, darkScrim)
        }
        activity.enableEdgeToEdge(
            statusBarStyle = statusBarStyle,
            navigationBarStyle = navigationBarStyle,
        )
        WindowCompat.getInsetsController(activity.window, view).systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    DisposableEffect(activity) {
        activity.window.applyAppImmersiveSystemBars()
        onDispose {
        }
    }
}

@Composable
fun getActivity(): Activity? {
    return LocalContext.current as? Activity
}
