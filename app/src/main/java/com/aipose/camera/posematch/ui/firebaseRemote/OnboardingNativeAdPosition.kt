package com.aipose.camera.posematch.ui.firebaseRemote

import kotlinx.serialization.Serializable

@Serializable(with = OnboardingNativeAdPosition.Serializer::class)
enum class OnboardingNativeAdPosition(val remoteValue: Long, val afterStepCount: Int?) {
    Off(0L, null),
    AfterFirstStep(1L, 1),
    AfterSecondStep(2L, 2),
    ;

    object Serializer : RemoteValueEnumSerializer<OnboardingNativeAdPosition>(
        serialName = "OnboardingNativeAdPosition",
        entries = OnboardingNativeAdPosition.entries,
        fallback = Off,
        remoteValueOf = OnboardingNativeAdPosition::remoteValue,
    )
}
