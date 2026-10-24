package com.aipose.camera.posematch.ui.firebaseRemote

import kotlinx.serialization.Serializable

@Serializable(with = BottomAdPosition.Serializer::class)
enum class BottomAdPosition(val remoteValue: Long) {
    Off(0L),
    BelowBottomBar(1L),
    AboveBottomBar(2L),
    ;

    object Serializer : RemoteValueEnumSerializer<BottomAdPosition>(
        serialName = "BottomAdPosition",
        entries = BottomAdPosition.entries,
        fallback = Off,
        remoteValueOf = BottomAdPosition::remoteValue,
    )
}
