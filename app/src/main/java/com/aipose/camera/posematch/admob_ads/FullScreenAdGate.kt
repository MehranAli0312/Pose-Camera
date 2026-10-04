package com.aipose.camera.posematch.admob_ads

import android.os.SystemClock
import android.util.Log

/**
 * Mutual exclusion for full-screen ads (interstitial vs App Open).
 *
 * Google policy: two full-screen ads must never overlap. The previous guard only looked at
 * boolean flags like `isInterstitialAdOnScreen` which were only set when the ad was already
 * visible. That left a window (bridge dialogs, loading states) where another ad could start.
 *
 * This gate is claimed for the WHOLE flow (from the moment a show is requested until it is fully
 * dismissed or has failed), and both ad types check it before showing.
 */
object FullScreenAdGate {

    private const val TAG = "FullScreenAdGate"

    /** Back-to-back full-screen ads feel broken and risk policy issues — enforce a short gap. */
    private const val COOLDOWN_MS = 1_000L

    /**
     * Safety net: if some path claims the gate and never releases it (crash, unexpected callback
     * ordering), ads would be blocked forever. Treat a claim older than this as stale.
     */
    private const val STALE_CLAIM_MS = 60_000L

    private const val OWNER_NONE = 0
    private const val OWNER_INTERSTITIAL = 1
    private const val OWNER_APP_OPEN = 2

    @Volatile
    private var owner = OWNER_NONE

    @Volatile
    private var claimedAt = 0L

    @Volatile
    private var releasedAt = 0L

    /** True while any full-screen ad flow owns the gate (including the pre-show bridge window). */
    val isAnyActive: Boolean
        get() = synchronized(this) { activeOwner() != OWNER_NONE }

    val isInterstitialActive: Boolean
        get() = synchronized(this) { activeOwner() == OWNER_INTERSTITIAL }

    val isAppOpenActive: Boolean
        get() = synchronized(this) { activeOwner() == OWNER_APP_OPEN }

    /** True during the short gap after a full-screen ad was dismissed. */
    val isInCooldown: Boolean
        get() = synchronized(this) {
            releasedAt != 0L && SystemClock.elapsedRealtime() - releasedAt < COOLDOWN_MS
        }

    fun beginInterstitial(): Boolean = claim(OWNER_INTERSTITIAL, "interstitial")

    fun endInterstitial() = release(OWNER_INTERSTITIAL, "interstitial")

    fun beginAppOpen(): Boolean = claim(OWNER_APP_OPEN, "appOpen")

    fun endAppOpen() = release(OWNER_APP_OPEN, "appOpen")

    private fun claim(newOwner: Int, label: String): Boolean = synchronized(this) {
        val current = activeOwner()
        if (current != OWNER_NONE) {
            Log.d(TAG, "$label blocked: ${name(current)} already owns the screen")
            return false
        }
        if (isInCooldown) {
            Log.d(TAG, "$label blocked: cooldown after the previous full-screen ad")
            return false
        }
        owner = newOwner
        claimedAt = SystemClock.elapsedRealtime()
        Log.d(TAG, "$label claimed the full-screen slot")
        return true
    }

    private fun release(expectedOwner: Int, label: String) {
        synchronized(this) {
            if (owner != expectedOwner) return@synchronized
            owner = OWNER_NONE
            claimedAt = 0L
            releasedAt = SystemClock.elapsedRealtime()
            Log.d(TAG, "$label released the full-screen slot")
        }
    }

    /** Owner, treating an over-old claim as stale so a missed release can't block ads forever. */
    private fun activeOwner(): Int {
        if (owner == OWNER_NONE) return OWNER_NONE
        if (SystemClock.elapsedRealtime() - claimedAt > STALE_CLAIM_MS) {
            Log.w(TAG, "stale ${name(owner)} claim discarded")
            owner = OWNER_NONE
            claimedAt = 0L
            return OWNER_NONE
        }
        return owner
    }

    private fun name(value: Int) = when (value) {
        OWNER_INTERSTITIAL -> "interstitial"
        OWNER_APP_OPEN -> "appOpen"
        else -> "none"
    }
}
