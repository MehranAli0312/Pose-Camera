package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.aipose.camera.posematch.ui.theme.PoseNightMid
import com.aipose.camera.posematch.ui.theme.PosePremiumWarmTop

private const val WARM_OVERLAY_HEIGHT_RATIO = 0.525f

fun Modifier.proPaywallBackground(): Modifier = this
    .background(PoseNightMid)
    .drawBehind {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(PosePremiumWarmTop, Color.Transparent),
                startY = 0f,
                endY = size.height * WARM_OVERLAY_HEIGHT_RATIO,
            ),
        )
    }
