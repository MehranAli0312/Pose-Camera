package com.aipose.camera.posematch.admob_ads.native_ad


import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NativeAdCache {
    private const val TAG = "NativeAdCache"
    private val adMap =
        LinkedHashMap<String, NativeAd>()

    @Synchronized
    fun save(key: String, ad: NativeAd) {
        Log.d(TAG, "save(): caching ad for key=$key")
        adMap.remove(key)?.let { oldAd ->
            destroyAdsAsync(listOf(oldAd))
        }
        adMap[key] = ad
    }

    @Synchronized
    fun getOnce(key: String): NativeAd? {
        val cachedAd = adMap.remove(key)
        if (cachedAd != null) {
            Log.d(TAG, "getOnce(): returning cached ad for key=$key")
        }
        return cachedAd
    }

    @Synchronized
    fun peek(key: String): NativeAd? {
        return adMap[key]
    }

    @Synchronized
    fun hasAd(key: String): Boolean = adMap.containsKey(key)

    @Synchronized
    fun clear(key: String) {
        adMap.remove(key)?.let { cachedAd ->
            destroyAdsAsync(listOf(cachedAd))
        }
    }

    @Synchronized
    fun clearAll() {
        if (adMap.isEmpty()) return

        val adsToDestroy = ArrayList<NativeAd>()
        adsToDestroy.addAll(adMap.values)
        adMap.clear()
        destroyAdsAsync(adsToDestroy)
    }

    private fun destroyAdsAsync(
        adsToDestroy: List<NativeAd>
    ) {
        if (adsToDestroy.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            adsToDestroy.forEach { ad ->
                try {
                    ad.destroy()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to destroy cached ad safely", e)
                }
            }
        }
    }
}
