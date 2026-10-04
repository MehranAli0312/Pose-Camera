package com.aipose.camera.posematch.admob_ads

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import com.aipose.camera.posematch.admob_ads.banner_ad.BannerAdConfig
import com.aipose.camera.posematch.admob_ads.banner_ad.BannerAdHelperNextGen
import com.aipose.camera.posematch.admob_ads.native_ad.NativeAdConfig
import com.aipose.camera.posematch.admob_ads.native_ad.NativeAdHelper
import com.aipose.camera.posematch.admob_ads.remote.NativeAdStyleManager
import com.aipose.camera.posematch.admob_ads.remote.RemoteConfig

/**
 * Renders a single bottom-of-screen ad slot as either a NATIVE or a BANNER, decided by a
 * Remote Config mode string ("native" | "banner" | anything else = off). All in-app screens reuse
 * one native id and one banner id; only the per-screen RC mode + layout variant differ.
 *
 * The shimmer (sized to the chosen native layout) shows first so the user perceives an ad of that
 * exact size loading.
 */
object ScreenBottomAd {

    /**
     * A rendered slot, whatever format filled it.
     *
     * Both formats are releasable, which matters for screens that open and close repeatedly inside
     * one Fragment lifetime (editor / photo preview / success): their lifecycleOwner outlives them
     * by a long way, so "it gets cleaned up on onDestroy" is not good enough — the caller must be
     * able to stop the refresh the moment the container view goes away.
     */
    fun interface Slot {
        fun release()
    }

    fun render(
        activity: Activity,
        owner: LifecycleOwner,
        holder: FrameLayout,
        label: View?,
        mode: String,
        placement: String,
        nativeId: String,
        bannerId: String,
        tag: String
    ): Slot? {
        val (contentLayout, shimmer) = NativeAdStyleManager.getAdLayouts(placement)
        return when (mode) {
            RemoteConfig.NATIVE_MODE -> {
                label?.visibility = View.VISIBLE
                holder.visibility = View.VISIBLE
                val helper = NativeAdHelper(
                    activity,
                    owner,
                    NativeAdConfig(
                        idAds = nativeId,
                        canReloadAds = RemoteConfig.reloadNativeAds,
                        layoutId = contentLayout,
                        shimmerLayout = shimmer,
                        TAG = tag
                    )
                ).also {
                    it.nativeContentView = holder
                    it.showShimmer()
                    it.loadAndShowNativeAd()
                }
                Slot { helper.release() }
            }

            RemoteConfig.BANNER_MODE -> {
                label?.visibility = View.VISIBLE
                holder.visibility = View.VISIBLE
                val helper = BannerAdHelperNextGen(
                    activity,
                    owner,
                    BannerAdConfig(bannerId, isFreeSizeAd = true)
                ).apply {
                    myView = holder
                    showBannerUsingShimmerHeight(shimmer)
                }
                Slot { helper.release() }
            }

            else -> {
                // "off" — no bottom ad on this screen.
                holder.visibility = View.GONE
                label?.visibility = View.GONE
                null
            }
        }
    }
}
