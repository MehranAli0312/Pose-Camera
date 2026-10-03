package com.aipose.camera.posematch.domain.models

import java.util.concurrent.TimeUnit

data class ProEntitlement(
    val isPro: Boolean,
    val isLifetime: Boolean,
    val verifiedAtMillis: Long,
) {

    fun isUsableOffline(nowMillis: Long): Boolean {
        if (!isPro) return false
        if (isLifetime) return true
        val age = nowMillis - verifiedAtMillis
        return age in 0..SUBSCRIPTION_OFFLINE_GRACE_MILLIS
    }

    companion object {
        val SUBSCRIPTION_OFFLINE_GRACE_MILLIS: Long = TimeUnit.DAYS.toMillis(3)
    }
}
