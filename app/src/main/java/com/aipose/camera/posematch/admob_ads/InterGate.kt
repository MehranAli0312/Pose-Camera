package com.aipose.camera.posematch.admob_ads

import androidx.fragment.app.Fragment
import com.aipose.camera.posematch.admob_ads.inter.showInterWithCounter
import com.aipose.camera.posematch.admob_ads.remote.RemoteConfig

/**
 * One entry point for every IN-APP interstitial placement.
 *
 * All of them share a single ad id (`InterHome`) and a single frequency counter
 * (`RemoteConfig.interAdsCounter`), so adding a placement never means adding an ad unit — it only
 * means adding a Remote Config boolean that can turn that one transition on or off.
 *
 * [placementEnabled] is the caller's already-ANDed gate: the master switch for its direction
 * (`enableInter` for forward navigation, `enableInterBack` for back presses) AND the placement's
 * own key. When the gate is off, or no ad is cached, or the counter has not come round yet,
 * [action] runs straight away — navigation must never be blocked by an ad.
 */
fun Fragment.showInterThen(placementEnabled: Boolean, action: () -> Unit) {
    if (RemoteConfig.enabledAllAds && placementEnabled && isAdded) {
        requireActivity().showInterWithCounter(closeListener = action, failListener = action)
    } else {
        action()
    }
}

/** Forward-navigation placement gate: master `enableInter` AND this placement's own RC key. */
fun forwardInter(placementKey: Boolean): Boolean = RemoteConfig.enableInter && placementKey

/** Back-press placement gate: master `enableInterBack` AND this placement's own RC key. */
fun backInter(placementKey: Boolean): Boolean = RemoteConfig.enableInterBack && placementKey
