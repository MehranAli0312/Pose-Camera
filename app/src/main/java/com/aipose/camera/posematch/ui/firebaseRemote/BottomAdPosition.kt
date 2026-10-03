package com.aipose.camera.posematch.ui.firebaseRemote

enum class BottomAdPosition(val remoteValue: Long) {
    Off(0L),
    AboveBottomBar(1L),
    BelowBottomBar(2L),
    ;

    companion object {
        fun fromRemote(value: Long): BottomAdPosition =
            entries.firstOrNull { it.remoteValue == value } ?: Off
    }
}
