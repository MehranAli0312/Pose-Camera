package com.aipose.camera.posematch.ui.common

import android.view.View
import android.view.ViewParent
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

@Composable
fun ImmersiveDialogWindowEffect() {
    val dialogWindow = LocalView.current.findDialogWindow()
    DisposableEffect(dialogWindow) {
        dialogWindow?.let { AppSystemBars(it).applyImmersiveBehavior() }
        onDispose { }
    }
}

private fun View.findDialogWindow(): Window? {
    var parent: ViewParent? = this.parent
    while (parent != null) {
        if (parent is DialogWindowProvider) return parent.window
        parent = parent.parent
    }
    return null
}
