package com.aipose.camera.posematch.ui.firebaseRemote

enum class OnboardingNativeAdPosition(val remoteValue: Long, val afterStepCount: Int?) {
    Off(0L, null),
    AfterFirstStep(1L, 1),
    AfterSecondStep(2L, 2),
    ;

    companion object {
        fun fromRemote(value: Long): OnboardingNativeAdPosition =
            entries.firstOrNull { it.remoteValue == value } ?: Off
    }
}
