package com.aipose.camera.posematch.ui.screens

// Top-level navigation routes.
object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LANGUAGE = "language"
    const val MAIN_CONTAINER = "main_container"
    const val STYLE_CATEGORY = "style_category/{category}"
}

// Sub-navigation tabs for the main container. Icons live in the XML bottom nav; the enum just
// carries the identity (HISTORY is shown to the user as "Collections").
enum class NavTab(val title: String, val tag: String) {
    HOME("Home", "tab_home"),
    HISTORY("Collections", "tab_history"),
    SETTINGS("Settings", "tab_settings")
}
