package com.aipose.camera.posematch.ui.firebaseRemote

enum class PremiumCloseButtonPosition(val remoteValue: Long) {
    Right(0L),
    Left(1L),
    ;

    companion object {
        fun fromRemote(value: Long): PremiumCloseButtonPosition =
            entries.firstOrNull { it.remoteValue == value } ?: Right
    }
}
