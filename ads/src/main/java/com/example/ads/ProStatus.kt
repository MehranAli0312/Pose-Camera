package com.example.ads

enum class ProStatus {

    UNKNOWN,

    PRO,

    FREE,
    ;

    val isEligibleForAds: Boolean get() = this == FREE

    val isResolved: Boolean get() = this != UNKNOWN
}
