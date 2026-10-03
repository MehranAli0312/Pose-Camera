package com.aipose.camera.posematch.domain.models

enum class NetworkTransport {
    WIFI,
    CELLULAR,
    ETHERNET,
    VPN,
    OTHER,
}

enum class NetworkIssue {

    AIRPLANE_MODE,

    NO_NETWORK,

    NO_INTERNET,

    CAPTIVE_PORTAL,
}

sealed interface NetworkStatus {

    data class Connected(
        val transport: NetworkTransport,
        val isMetered: Boolean,
    ) : NetworkStatus

    data class Unavailable(val issue: NetworkIssue) : NetworkStatus

    val isConnected: Boolean get() = this is Connected
}
