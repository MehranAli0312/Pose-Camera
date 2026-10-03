package com.aipose.camera.posematch.domain.models

enum class AppThemeOption(val key: String) {
    Dark("Dark"),
    Light("Light"),
    SleekCharcoal("Sleek Charcoal"),
    CyberpunkViolet("Cyberpunk Violet");

    companion object {
        private val byKey = entries.associateBy { it.key }

        fun fromKey(key: String?): AppThemeOption = byKey[key] ?: Dark
    }
}
