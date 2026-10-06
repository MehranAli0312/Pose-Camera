package com.aipose.camera.posematch.ui.screens.bottomBar

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp

@Immutable
internal data class DashboardBottomAdSpace(
    val isLive: Boolean,
    val height: Dp,
    val onHeightChanged: (Dp) -> Unit = {},
)
