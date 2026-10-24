package com.aipose.camera.posematch.ui.firebaseRemote

import kotlinx.serialization.Serializable

@Serializable(with = ReturningSessionPremium.Serializer::class)
enum class ReturningSessionPremium(val remoteValue: Long) {
    Off(0L),
    AfterSplash(1L),
    ;

    object Serializer : RemoteValueEnumSerializer<ReturningSessionPremium>(
        serialName = "ReturningSessionPremium",
        entries = ReturningSessionPremium.entries,
        fallback = Off,
        remoteValueOf = ReturningSessionPremium::remoteValue,
    )
}
