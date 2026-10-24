package com.aipose.camera.posematch.ui.firebaseRemote

import kotlinx.serialization.Serializable

@Serializable(with = PremiumFeatureDialogMode.Serializer::class)
enum class PremiumFeatureDialogMode(val remoteValue: Long) {
    Off(0L),
    On(1L),
    ;

    object Serializer : RemoteValueEnumSerializer<PremiumFeatureDialogMode>(
        serialName = "PremiumFeatureDialogMode",
        entries = PremiumFeatureDialogMode.entries,
        fallback = Off,
        remoteValueOf = PremiumFeatureDialogMode::remoteValue,
    )
}
