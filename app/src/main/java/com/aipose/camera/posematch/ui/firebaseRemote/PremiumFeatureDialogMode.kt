package com.aipose.camera.posematch.ui.firebaseRemote

enum class PremiumFeatureDialogMode(val remoteValue: Long) {
    Off(0L),
    On(1L),
    ;

    companion object {
        fun fromRemote(value: Long): PremiumFeatureDialogMode =
            entries.firstOrNull { it.remoteValue == value } ?: Off
    }
}
