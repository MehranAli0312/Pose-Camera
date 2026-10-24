package com.aipose.camera.posematch.ui.firebaseRemote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppUpdateRemoteConfig(
    @SerialName("version_code") val versionCode: Long = 0L,
    @SerialName("force_update") val forceUpdate: Boolean = false,
)
