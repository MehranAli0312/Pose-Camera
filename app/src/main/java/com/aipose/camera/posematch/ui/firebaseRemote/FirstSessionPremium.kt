package com.aipose.camera.posematch.ui.firebaseRemote

import kotlinx.serialization.Serializable

@Serializable(with = FirstSessionPremium.Serializer::class)
enum class FirstSessionPremium(val remoteValue: Long) {
    Off(0L),
    AfterSplash(1L),
    AfterLanguage(2L),
    AfterOnboarding(3L),
    ;

    object Serializer : RemoteValueEnumSerializer<FirstSessionPremium>(
        serialName = "FirstSessionPremium",
        entries = FirstSessionPremium.entries,
        fallback = Off,
        remoteValueOf = FirstSessionPremium::remoteValue,
    )
}
