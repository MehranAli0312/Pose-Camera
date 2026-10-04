package com.aipose.camera.posematch.admob_ads.native_ad

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.libraries.ads.mobile.sdk.common.VideoOptions
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.aipose.camera.posematch.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView
import com.aipose.camera.posematch.admob_ads.ensureMobileAdsSdkInitialized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import com.aipose.camera.posematch.analytics.Analytics

class NativeAdHelper(
    val activity: Activity, val lifecycleOwner: LifecycleOwner, val config: NativeAdConfig
) : LifecycleEventObserver {

    private var autoReloadJob: Job? = null

    enum class AdState { LOADING, LOADED, FAILED }
    enum class LifeCycleStates { OnResume, OnPause, OnDestroy, OnStart, OnStop, OnCreate }

    var currentLifeCycleState = LifeCycleStates.OnResume
    var adState = AdState.LOADED
    var isActivityPaused = false
    var isAdShowed = false
    private var isResumeEnabled = false

    var counterAdsLoading = 0
    private var tag = "NativeAdHelper12"
    var nativeContentView: FrameLayout? = null
    var shimmerLayoutView: ShimmerFrameLayout? = null

    private val isLoading = AtomicBoolean(false)
    private var lastShowTime: Long = 0L
    private val minShowAfterShowMs = 2_000L

    /**
     * Invoked on the main thread the moment the ad view is actually inflated, populated and
     * attached (i.e. the user can see the ad, not the shimmer). Used by the splash to keep the
     * native visible for a minimum time before covering it with the interstitial.
     */
    var onAdDisplayed: (() -> Unit)? = null

    /**
     * Ad layouts use MaterialComponents widgets (MaterialCardView), which require a
     * MaterialComponents theme. The app theme is DeviceDefault-based, so inflating with the raw
     * activity threw IllegalArgumentException on every ad — the exception was swallowed and the
     * shimmer stayed forever. Inflate through a Material-themed wrapper instead.
     */
    private val adInflater: LayoutInflater by lazy {
        LayoutInflater.from(ContextThemeWrapper(activity, R.style.Theme_PoseMatch_Ads))
    }

    @Volatile
    private var ownerDestroyed = false

    private var currentNativeAd: NativeAd? = null
    private var shimmerRootView: View? = null

    private val lifecycleObserver: DefaultLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            Log.d(tag, "Lifecycle: onCreate")
            currentLifeCycleState = LifeCycleStates.OnCreate
        }

        override fun onPause(owner: LifecycleOwner) {
            Log.d(tag, "Lifecycle: onPause")
            currentLifeCycleState = LifeCycleStates.OnPause
            isActivityPaused = true
            autoReloadJob?.cancel()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            Log.d(tag, "Lifecycle: onDestroy")
            currentLifeCycleState = LifeCycleStates.OnDestroy
            ownerDestroyed = true
            autoReloadJob?.cancel()
            destroyCurrentAd()
        }

        override fun onStart(owner: LifecycleOwner) {
            Log.d(tag, "Lifecycle: onStart")
            currentLifeCycleState = LifeCycleStates.OnStart
        }

        override fun onStop(owner: LifecycleOwner) {
            Log.d(tag, "Lifecycle: onStop")
            currentLifeCycleState = LifeCycleStates.OnStop
        }

        override fun onResume(owner: LifecycleOwner) {
            Log.d(tag, "Lifecycle: onResume (isResumeEnabled: $isResumeEnabled)")
            currentLifeCycleState = LifeCycleStates.OnResume
            isActivityPaused = false
            if (!isResumeEnabled) return
            NativeAdCache.getOnce(placementCacheKey())?.let { cached ->
                Log.d(tag, "onResume: Found cached ad, showing it")
                CoroutineScope(Dispatchers.Main).launch {
                    showLoadedNativeAd(cached)
                }
                return
            }

            if (!isAdShowed) {
                Log.d(tag, "onResume: Ad not showed yet, loading...")
                if (config.remoteHighFloorEnabled && config.highFloorAdId != null) {
                    loadAndShowNativeAdWithHighFloor()
                } else {
                    loadAndShowNativeAd()
                }
            } else if (config.canReloadAds) {
                Log.d(tag, "onResume: Ad already showed, resuming reload timer")
                startAutoReloadTimer()
            }
        }
    }

    init {
        tag = config.TAG
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
    }

    private fun placementCacheKey(adUnitId: String? = null): String {
        return config.cacheKey ?: config.idAds ?: adUnitId ?: config.highFloorAdId ?: config.TAG
    }

    private fun destroyCurrentAd() {
        Log.d(tag, "destroyCurrentAd: Destroying existing ad")
        currentNativeAd?.destroy()
        currentNativeAd = null
    }

    /**
     * Detach from the lifecycle and free the current ad. Call when swapping this helper out (e.g.
     * changing tabs) so it stops observing / auto-reloading and doesn't leak.
     */
    fun release() {
        autoReloadJob?.cancel()
        ownerDestroyed = true
        try {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        } catch (_: Exception) {
        }
        destroyCurrentAd()
        nativeContentView = null
    }

    fun requestAd() {
        Log.d(tag, "requestAd: Explicit ad request")
        loadAndShowNativeAd(explicitReload = true)
    }

    private fun showShimmerOnUi() {
        if (config.shimmerLayout == null || nativeContentView == null) return
        if (activity.isFinishing || activity.isDestroyed) return

        activity.runOnUiThread {
            val container = nativeContentView ?: return@runOnUiThread
            val existingShimmer = shimmerRootView

            if (existingShimmer?.parent === container) {
                shimmerLayoutView?.visibility = View.VISIBLE
                shimmerLayoutView?.startShimmer()
                return@runOnUiThread
            }

            val shimmerView = adInflater.inflate(config.shimmerLayout!!, container, false)
            shimmerRootView = shimmerView
            shimmerLayoutView = shimmerView.findViewById(R.id.shimmer_container_native)
            container.removeAllViews()
            container.visibility = View.VISIBLE
            container.addView(shimmerView)
            shimmerLayoutView?.visibility = View.VISIBLE
            shimmerLayoutView?.startShimmer()
        }
    }

    /**
     * No ad will be shown — stop and REMOVE the shimmer, then collapse the slot. Previously this
     * only hid the inner ShimmerFrameLayout, so a failed load left a dead placeholder on screen.
     */
    private fun hideShimmerOnUi() {
        activity.runOnUiThread {
            shimmerLayoutView?.stopShimmer()
            shimmerLayoutView?.visibility = View.GONE
            nativeContentView?.let { container ->
                shimmerRootView?.let { container.removeView(it) }
                if (container.childCount == 0) container.visibility = View.GONE
            }
            shimmerLayoutView = null
            shimmerRootView = null
        }
    }

    private fun loadAndReturnAd(
        activity: Activity,
        nativeId: String,
        showShimmer: Boolean = true,
        adResult: ((NativeAd?) -> Unit)
    ) {
        if (!isLoading.compareAndSet(false, true)) {
            Log.d(tag, "loadAndReturnAd: Already loading, skipping request for $nativeId")
            adResult.invoke(null)
            return
        }

        if (isActivityPaused) {
            Log.d(tag, "loadAndReturnAd: Activity paused, failing load for $nativeId")
            adState = AdState.FAILED
            isLoading.set(false)
            hideShimmerOnUi()
            adResult.invoke(null)
            return
        }

        NativeAdCache.getOnce(placementCacheKey(nativeId))?.let { cached ->
            Log.d(tag, "loadAndReturnAd: Found cached ad for $nativeId")
            adState = AdState.LOADED
            isLoading.set(false)
            adResult.invoke(cached)
            return
        }

        Log.d(tag, "loadAndReturnAd: Starting fresh load for $nativeId")
        Analytics.ad(Analytics.AdAction.REQUESTED, "native", tag)
        adState = AdState.LOADING
        if (showShimmer) {
            showShimmerOnUi()
        }

        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()

        if (!activity.ensureMobileAdsSdkInitialized(tag, "loadAndReturnAd")) {
            adState = AdState.FAILED
            isLoading.set(false)
            hideShimmerOnUi()
            adResult.invoke(null)
            return
        }

        val adRequest = NativeAdRequest.Builder(nativeId, listOf(NativeAd.NativeAdType.NATIVE))
            .setVideoOptions(videoOptions).build()

        NativeAdLoader.load(adRequest, object : NativeAdLoaderCallback {
            override fun onNativeAdLoaded(nativeAd: NativeAd) {
                Analytics.ad(Analytics.AdAction.LOADED, "native", tag)
                adState = AdState.LOADED
                if (ownerDestroyed || activity.isDestroyed || activity.isFinishing) {
                    NativeAdCache.save(placementCacheKey(nativeId), nativeAd)
                    isLoading.set(false)
                    hideShimmerOnUi()
                    adResult.invoke(null)
                    return
                }
                isLoading.set(false)
                adResult.invoke(nativeAd)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                adState = AdState.FAILED
                isLoading.set(false)
                hideShimmerOnUi()
                Log.e(tag, "onAdFailedToLoad: ${adError.message}")
                Analytics.ad(Analytics.AdAction.FAILED, "native", tag, adError.message)
                adResult.invoke(null)
            }
        })
    }

    fun loadAndShowNativeAd(explicitReload: Boolean = false) {
        if (isLoading.get()) {
            Log.d(tag, "loadAndShowNativeAd: Already loading")
            return
        }
        isResumeEnabled = true
        if (!explicitReload) {
            showShimmerOnUi()
        }

        if (explicitReload && lastShowTime != 0L) {
            val elapsed = System.currentTimeMillis() - lastShowTime
            if (elapsed < minShowAfterShowMs) {
                Log.d(tag, "loadAndShowNativeAd: Throttling reload (elapsed: $elapsed ms)")
                return
            }
        }

        if (isAdShowed && !config.canReloadAds) {
            Log.d(tag, "loadAndShowNativeAd: Ad already showed and reload is disabled")
            return
        }

        counterAdsLoading++
        val adId = config.idAds

        NativeAdCache.getOnce(placementCacheKey(adId))?.let { cachedAd ->
            Log.d(tag, "loadAndShowNativeAd: Found cached ad, showing it")
            CoroutineScope(Dispatchers.Main).launch {
                showLoadedNativeAd(cachedAd)
            }
            return
        }
        if (adId == null) {
            Log.e(tag, "loadAndShowNativeAd: Ad ID is null")
            hideShimmerOnUi()
            return
        }

        Log.d(tag, "loadAndShowNativeAd: Loading ad with ID: $adId")
        CoroutineScope(Dispatchers.IO).launch {
            loadAndReturnAd(activity, adId, showShimmer = !explicitReload) { nativeAd ->
                nativeAd?.let { ad ->
                    CoroutineScope(Dispatchers.Main).launch {
                        if (ownerDestroyed || activity.isDestroyed || activity.isFinishing || currentLifeCycleState != LifeCycleStates.OnResume || nativeContentView == null) {
                            Log.d(tag, "loadAndShowNativeAd: Activity/Owner not in state to show, caching ad. State: $currentLifeCycleState, Container: ${nativeContentView != null}")
                            NativeAdCache.save(placementCacheKey(adId), ad)
                            return@launch
                        }
                        showAdOnUI(ad)
                    }
                }
            }
        }
    }

    fun loadAndShowNativeAdWithHighFloor(explicitReload: Boolean = false) {
        if (isLoading.get()) {
            Log.d(tag, "loadAndShowNativeAdWithHighFloor: Already loading")
            return
        }
        isResumeEnabled = true
        if (!explicitReload) {
            showShimmerOnUi()
        }

        if (explicitReload && lastShowTime != 0L) {
            val elapsed = System.currentTimeMillis() - lastShowTime
            if (elapsed < minShowAfterShowMs) {
                Log.d(tag, "loadAndShowNativeAdWithHighFloor: Throttling reload (elapsed: $elapsed ms)")
                return
            }
        }

        if (isAdShowed && !config.canReloadAds) {
            Log.d(tag, "loadAndShowNativeAdWithHighFloor: Ad already showed and reload is disabled")
            return
        }

        NativeAdCache.getOnce(placementCacheKey())?.let { cachedAd ->
            Log.d(tag, "loadAndShowNativeAdWithHighFloor: Found cached ad, showing it")
            CoroutineScope(Dispatchers.Main).launch {
                showLoadedNativeAd(cachedAd)
            }
            return
        }

        val normalAdId = config.idAds
        val highAdId = config.highFloorAdId

        if (highAdId.isNullOrBlank() || !config.remoteHighFloorEnabled) {
            Log.d(tag, "loadAndShowNativeAdWithHighFloor: High floor disabled or missing ID, falling back to normal load")
            normalAdId?.let { id ->
                CoroutineScope(Dispatchers.IO).launch {
                    loadAndReturnAd(activity, id, showShimmer = !explicitReload) { ad ->
                        ad?.let { showAdSafely(it) }
                    }
                }
            } ?: run {
                hideShimmerOnUi()
            }
            return
        }

        Log.d(tag, "loadAndShowNativeAdWithHighFloor: Loading high floor ad: $highAdId")
        CoroutineScope(Dispatchers.IO).launch {
            loadAndReturnAd(activity, highAdId, showShimmer = !explicitReload) { highAd ->
                if (highAd != null) {
                    Log.d(tag, "loadAndShowNativeAdWithHighFloor: High floor ad loaded")
                    showAdSafely(highAd)
                } else {
                    Log.d(tag, "loadAndShowNativeAdWithHighFloor: High floor ad failed, trying normal floor: $normalAdId")
                    normalAdId?.let { id ->
                        loadAndReturnAd(activity, id, showShimmer = !explicitReload) { normalAd ->
                            normalAd?.let {
                                showAdSafely(
                                    normalAd
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showAdSafely(ad: NativeAd) {
        CoroutineScope(Dispatchers.Main).launch {
            val invalid =
                ownerDestroyed || activity.isDestroyed || activity.isFinishing || !lifecycleOwner.lifecycle.currentState.isAtLeast(
                    Lifecycle.State.STARTED
                ) || nativeContentView == null

            if (invalid) {
                Log.d(tag, "showAdSafely: State invalid for showing, caching ad. Container: ${nativeContentView != null}")
                NativeAdCache.save(placementCacheKey(), ad)
                return@launch
            }
            showAdOnUI(ad)
        }
    }

    private fun showAdOnUI(ad: NativeAd) {
        try {
            if (!activity.ensureMobileAdsSdkInitialized(tag, "showAdOnUI")) {
                hideShimmerOnUi()
                NativeAdCache.save(placementCacheKey(), ad)
                return
            }

            if (nativeContentView == null) {
                Log.e(tag, "showAdOnUI: nativeContentView is NULL, caching ad")
                hideShimmerOnUi()
                NativeAdCache.save(placementCacheKey(), ad)
                return
            }

            Log.d(tag, "showAdOnUI: Inflating and showing ad")
            val adView = adInflater.inflate(config.layoutId, null, false) as NativeAdView

            nativeContentView?.let { container ->
                currentNativeAd?.takeIf { it !== ad }?.destroy()
                container.removeAllViews()
                container.visibility = View.VISIBLE
                container.addView(adView)

                populateUnifiedNativeAdView(ad, adView)

                isAdShowed = true
                lastShowTime = System.currentTimeMillis()
                currentNativeAd = ad
                onAdDisplayed?.invoke()
                Analytics.ad(Analytics.AdAction.SHOWN, "native", tag)

                if (config.canReloadAds && config.reloadTime > 0) {
                    startAutoReloadTimer()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error showing ad", e)
            hideShimmerOnUi()
            NativeAdCache.save(placementCacheKey(), ad)
        }
    }

    fun showLoadedNativeAd(nativeAd: NativeAd) {
        isResumeEnabled = true
        val invalid =
            ownerDestroyed || activity.isDestroyed || activity.isFinishing || !lifecycleOwner.lifecycle.currentState.isAtLeast(
                Lifecycle.State.STARTED
            ) || nativeContentView == null

        if (invalid) {
            NativeAdCache.save(placementCacheKey(), nativeAd)
            return
        }
        showAdOnUI(nativeAd)
    }

    private fun populateUnifiedNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        shimmerLayoutView?.stopShimmer()
        shimmerLayoutView?.visibility = View.GONE
        shimmerRootView = null

        // Map views from layout
        val adMedia: MediaView? = adView.findViewById(R.id.ad_media)
        val adHeadline: TextView? = adView.findViewById(R.id.ad_headline)
        val adBody: TextView? = adView.findViewById(R.id.ad_body)
        val adCallToAction: View? = adView.findViewById(R.id.ad_call_to_action)
        val adIcon: ImageView? = adView.findViewById(R.id.ad_icon)
        val adPrice: TextView? = adView.findViewById(R.id.ad_price)
        val adStars: RatingBar? = adView.findViewById(R.id.ad_stars)
        val adStore: TextView? = adView.findViewById(R.id.ad_store)
        val adAdvertiser: TextView? = adView.findViewById(R.id.ad_advertiser)

        // Assign views to NativeAdView for tracking
        adHeadline?.let { adView.headlineView = it }
        adBody?.let { adView.bodyView = it }
        adCallToAction?.let { adView.callToActionView = it }
        adIcon?.let { adView.iconView = it }
        adPrice?.let { adView.priceView = it }
        adStars?.let { adView.starRatingView = it }
        adStore?.let { adView.storeView = it }
        adAdvertiser?.let { adView.advertiserView = it }

        // Populate assets
        adHeadline?.apply {
            text = nativeAd.headline
            visibility = if (nativeAd.headline.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        adBody?.apply {
            text = nativeAd.body
            visibility = if (nativeAd.body.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        adCallToAction?.apply {
            val cta = nativeAd.callToAction
            if (cta.isNullOrEmpty()) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                if (this is Button) {
                    text = cta
                } else if (this is TextView) {
                    text = cta
                }
            }
        }

        adPrice?.apply {
            text = nativeAd.price
            visibility = if (nativeAd.price.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        adStore?.apply {
            text = nativeAd.store
            visibility = if (nativeAd.store.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        adAdvertiser?.apply {
            text = nativeAd.advertiser
            visibility = if (nativeAd.advertiser.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        adIcon?.apply {
            if (nativeAd.icon != null) {
                setImageDrawable(nativeAd.icon?.drawable)
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        adStars?.apply {
            if (nativeAd.starRating != null) {
                rating = nativeAd.starRating?.toFloat() ?: 0f
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        // Handle MediaView
        adMedia?.apply {
            visibility = View.VISIBLE
        }

        // Important: Register the ad with the view.
        adView.registerNativeAd(nativeAd, adMedia)

        nativeAd.adEventCallback = object : NativeAdEventCallback {
            override fun onAdImpression() {
                Log.d(tag, "onAdImpression")
                Analytics.ad(Analytics.AdAction.IMPRESSION, "native", tag)
            }

            override fun onAdClicked() {
                Log.d(tag, "onAdClicked")
                Analytics.ad(Analytics.AdAction.CLICKED, "native", tag)
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {}

    fun showShimmer() {
        showShimmerOnUi()
    }

    fun loadNativeAd(
        onAdLoaded: (NativeAd?) -> Unit
    ) {
        loadAndReturnAd(activity, config.idAds ?: "") { ad ->
            onAdLoaded(ad)
        }
    }

    fun showNativeAd() {
        loadAndShowNativeAd(false)
    }

    private fun startAutoReloadTimer() {
        autoReloadJob?.cancel()
        if (!config.canReloadAds || config.reloadTime <= 0L) {
            return
        }

        autoReloadJob = CoroutineScope(Dispatchers.Main).launch {
            val elapsedSinceLastShow = if (lastShowTime == 0L) 0L else System.currentTimeMillis() - lastShowTime
            val delayForReload = (config.reloadTime - elapsedSinceLastShow).coerceAtLeast(0L)
            delay(delayForReload)
            if (!ownerDestroyed &&
                !activity.isDestroyed &&
                !activity.isFinishing &&
                currentLifeCycleState == LifeCycleStates.OnResume &&
                !isActivityPaused &&
                lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            ) {
                Log.d(tag, "startAutoReloadTimer: Reloading ad after ${config.reloadTime}ms")
                if (config.remoteHighFloorEnabled && config.highFloorAdId != null) {
                    loadAndShowNativeAdWithHighFloor(explicitReload = true)
                } else {
                    loadAndShowNativeAd(explicitReload = true)
                }
            }
        }
    }
}
