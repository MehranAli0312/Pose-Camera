package com.aipose.camera.posematch.ui.screens

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.aipose.camera.posematch.ui.theme.AppThemeState

// Theme-driven palette. These read from AppThemeState so switching themes re-grades the whole app.
val StudioBackgroundGradient: Brush
    get() = Brush.verticalGradient(listOf(AppThemeState.bgTop, AppThemeState.bgBottom))
val AccentCopper: Color get() = AppThemeState.accent
val SoftCardGray: Color get() = AppThemeState.card
val GlassWhite: Color get() = AppThemeState.glass
