package com.aipose.camera.posematch.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.*

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LANGUAGE = "language"
    const val MAIN_CONTAINER = "main_container"
}

/** Bottom-bar destinations inside [MainScenicContainer]. */
enum class NavTab(val title: String, val icon: ImageVector, val tag: String) {
    HOME("Home", Icons.Default.Home, "tab_home"),

    // Captured-photo album (History screen), shown to the user as "Collections".
    HISTORY("Collections", Icons.Default.Collections, "tab_history"),
    SETTINGS("Settings", Icons.Default.Settings, "tab_settings")
}
