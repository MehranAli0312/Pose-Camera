package com.aipose.camera.posematch.ui.firebaseRemote

import kotlinx.serialization.Serializable

@Serializable(with = PremiumCloseButtonPosition.Serializer::class)
enum class PremiumCloseButtonPosition(val remoteValue: Long) {
    Right(0L),
    Left(1L),
    ;

    object Serializer : RemoteValueEnumSerializer<PremiumCloseButtonPosition>(
        serialName = "PremiumCloseButtonPosition",
        entries = PremiumCloseButtonPosition.entries,
        fallback = Right,
        remoteValueOf = PremiumCloseButtonPosition::remoteValue,
    )
}
