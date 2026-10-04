package com.aipose.camera.posematch.ui.screens.home.models

import androidx.compose.runtime.Immutable

@Immutable
data class HomeFilter(
    val categories: List<String>,
    val selectedCategory: String?,
    val difficulties: List<String>,
    val selectedDifficulty: String?
)
