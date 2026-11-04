package com.aipose.camera.posematch.ui.screens.pro.models

import androidx.compose.ui.geometry.Offset

data class ProPoseSkeleton(
    val joints: List<Offset>,
    val bones: List<Pair<Int, Int>>,
)
