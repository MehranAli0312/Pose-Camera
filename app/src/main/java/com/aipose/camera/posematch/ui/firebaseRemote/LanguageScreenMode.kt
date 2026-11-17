package com.aipose.camera.posematch.ui.firebaseRemote

import kotlinx.serialization.Serializable

@Serializable(with = LanguageScreenMode.Serializer::class)
enum class LanguageScreenMode(val remoteValue: Long) {
    Off(0L),
    FirstSession(1L),
    SecondSession(2L),
    ;

    object Serializer : RemoteValueEnumSerializer<LanguageScreenMode>(
        serialName = "LanguageScreenMode",
        entries = entries,
        fallback = Off,
        remoteValueOf = LanguageScreenMode::remoteValue,
    )
}
