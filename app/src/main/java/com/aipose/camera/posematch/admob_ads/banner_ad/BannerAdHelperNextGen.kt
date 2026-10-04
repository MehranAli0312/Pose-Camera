package com.aipose.camera.posematch.admob_ads.banner_ad

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.aipose.camera.posematch.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.mediation.admob.AdMobAdapter
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRefreshCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.aipose.camera.posematch.admob_ads.ensureMobileAdsSdkInitialized
import kotlin.math.roundToInt
import com.aipose.camera.posematch.analytics.Analytics

class BannerAdHelperNextGen(
    private val activity: Activity,
    lifecycleOwner: LifecycleOwner,
    private val config: BannerAdConfig
)
{

    private val TAG = "BannerAdHelperNextGen"

    var myView: FrameLayout? = null
    var shimmer: ShimmerFrameLayout? = null

    /**
     * Invoked on the main thread once the banner view is actually attached and visible (not the
     * shimmer). The splash uses this to keep the banner uncovered for a minimum time before the
     * interstitial takes over — same contract as NativeAdHelper.onAdDisplayed.
     */
    var onAdDisplayed: (() -> Unit)? = null

    private var shimmerLayoutId: Int = R.layout.native_layout1_loading
    private var currentBannerAd: BannerAd? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private var trackedAdView: View? = null
    private var visibilityCheckRunnable: Runnable? = null
    private var visibleForFiveSecRunnable: Runnable? = null
    private var isViewTrackingActive = false
    private var lastAdViewedSatisfied = false

    enum class AdState {
        LOADING,
        LOADED,
        FAILED
    }

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            if (config.canReloadAds) {
                performReloadFlow()
            }
        }

        override fun onDestroy(owner: LifecycleOwner) {
            destroyCurrentAd()
        }
    }

    // Held so release() can detach the observer early, not just at owner onDestroy.
    private val ownerLifecycle = lifecycleOwner.lifecycle

    init {
        ownerLifecycle.addObserver(lifecycleObserver)
    }

    /**
     * Tear this banner down NOW, without waiting for the owning lifecycle to be destroyed.
     *
     * Screens that come and go inside a single Fragment lifetime (the editor, the photo
     * preview, the success overlay) share one viewLifecycleOwner, so onDestroy fires long
     * after their container view is gone. Without this, a closed overlay left a banner
     * refreshing against a detached FrameLayout — paid impressions nobody could see.
     */
    fun release() {
        ownerLifecycle.removeObserver(lifecycleObserver)
        destroyCurrentAd()
        shimmer?.stopShimmer()
        shimmer = null
        myView?.removeAllViews()
        myView = null
        onAdDisplayed = null
    }

    private fun performReloadFlow() {
        if (lastAdViewedSatisfied) {
            lastAdViewedSatisfied = false
            showBannerAdmob()
        }
    }

    fun showBannerAdmob() {
        if (config.isCollapsibleAd) {
            loadAndShowCollapsibleBannerAd()
        } else {
            val adView = AdView(activity)
            val adSize = getAdSize(activity)
            myView?.let { loadBannerWithView(adView, adSize, it) }
        }
    }

    private fun loadAndShowCollapsibleBannerAd() {
        val extras = Bundle()
        extras.putString("collapsible", "bottom")

        val adView = AdView(activity)
        val adSize = getAdSize(activity)

        myView?.let { loadBannerWithView(adView, adSize, it, extras) }
    }

    private fun loadBannerWithView(
        adView: AdView,
        adSize: AdSize,
        container: FrameLayout,
        extras: Bundle? = null
    ) {
        if (!activity.ensureMobileAdsSdkInitialized(TAG, "loadBannerWithView")) {
            return
        }

        val requestBuilder = BannerAdRequest.Builder(config.idAds, adSize)

        extras?.let {
            requestBuilder.putAdSourceExtrasBundle(AdMobAdapter::class.java, it)
        }

        adView.loadAd(
            requestBuilder.build(),
            object : AdLoadCallback<BannerAd> {
                override fun onAdLoaded(ad: BannerAd) {
                    activity.runOnUiThread {
                        currentBannerAd = ad

                        container.removeAllViews()
                        container.addView(adView)

                        adView.registerBannerAd(ad, activity)
                        setupAdCallbacks(ad)
                        onAdDisplayed?.invoke()
                                    Analytics.ad(Analytics.AdAction.SHOWN, "banner", "bottom")

                        Log.d(TAG, "Banner loaded successfully")
                        Analytics.ad(Analytics.AdAction.LOADED, "banner", "bottom")
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    activity.runOnUiThread {
                        Log.e(TAG, "Banner failed to load: ${error.message}")
                        Analytics.ad(Analytics.AdAction.FAILED, "banner", "bottom", error.message)
                    }
                }
            }
        )
    }

    private fun setupAdCallbacks(ad: BannerAd) {
        ad.adEventCallback = object : BannerAdEventCallback {
            override fun onAdImpression() {
                Log.d(TAG, "Banner impression")
                Analytics.ad(Analytics.AdAction.IMPRESSION, "banner", "bottom")
            }

            override fun onAdClicked() {
                Log.d(TAG, "Banner clicked")
                Analytics.ad(Analytics.AdAction.CLICKED, "banner", "bottom")
            }
        }

        ad.bannerAdRefreshCallback = object : BannerAdRefreshCallback {
            override fun onAdRefreshed() {
                Log.d(TAG, "Ad refreshed")
            }

            override fun onAdFailedToRefresh(error: LoadAdError) {
                Log.e(TAG, "Ad failed to refresh: ${error.message}")
            }
        }
    }

    fun showBannerUsingShimmerHeight(shimmerId: Int) {
        Log.d(TAG, "showBannerUsingShimmerHeight: called with id $shimmerId")

        shimmerLayoutId = shimmerId

        val container = myView
        if (container == null) {
            Log.e(TAG, "showBannerUsingShimmerHeight: myView is NULL")
            return
        }

        container.visibility = View.VISIBLE

        val inflater = LayoutInflater.from(activity)
        val shimmerRoot = inflater.inflate(shimmerLayoutId, container, false)

        val shimmerFrame = findFirstShimmer(shimmerRoot) ?: if (shimmerRoot is ShimmerFrameLayout) {
            shimmerRoot
        } else {
            shimmerRoot.findViewById(R.id.shimmer_container_banner)
        }

        shimmer = shimmerFrame

        container.removeAllViews()
        container.addView(shimmerRoot)

        shimmerFrame?.startShimmer()

        shimmerRoot.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    shimmerRoot.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val widthDp = getContainerWidthDp(container)
                    val shimmerHeightDp = getShimmerHeightDp(shimmerRoot, container)

                    Log.d(
                        TAG,
                        "showBannerUsingShimmerHeight: calculated widthDp=$widthDp, shimmerHeightDp=$shimmerHeightDp"
                    )

                    val adSize = if (config.isFreeSizeAd) {
                        AdSize(widthDp, shimmerHeightDp)
                    } else {
                        AdSize.getInlineAdaptiveBannerAdSize(widthDp, shimmerHeightDp)
                    }

                    val adRequest = BannerAdRequest.Builder(config.idAds, adSize).build()
                    val adView = AdView(activity)

                    adView.layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(shimmerHeightDp)
                    )

                    if (!activity.ensureMobileAdsSdkInitialized(TAG, "showBannerUsingShimmerHeight")) {
                        shimmerFrame?.stopShimmer()
                        container.removeAllViews()
                        return
                    }

                    adView.loadAd(
                        adRequest,
                        object : AdLoadCallback<BannerAd> {
                            override fun onAdLoaded(ad: BannerAd) {
                                activity.runOnUiThread {
                                    currentBannerAd = ad
                                    shimmerFrame?.stopShimmer()
                                    container.removeAllViews()
                                    container.addView(adView)
                                    adView.registerBannerAd(ad, activity)
                                    setupAdCallbacks(ad)
                                    onAdDisplayed?.invoke()
                                    Analytics.ad(Analytics.AdAction.SHOWN, "banner", "bottom")
                                }
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                activity.runOnUiThread {
                                    shimmerFrame?.stopShimmer()
                                    container.removeAllViews()
                                }
                            }
                        }
                    )
                }
            }
        )
    }

    private fun destroyCurrentAd() {
        currentBannerAd = null
        stopViewTracking()
    }

    private fun getAdSize(context: Context): AdSize {
        return AdSize.getLargeAnchoredAdaptiveBannerAdSize(
            context,
            getScreenWidthDp(context)
        )
    }

    private fun getScreenWidthDp(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return (displayMetrics.widthPixels / displayMetrics.density).roundToInt()
    }

    private fun getContainerWidthDp(container: FrameLayout): Int {
        val widthPx = when {
            container.width > 0 -> container.width
            container.measuredWidth > 0 -> container.measuredWidth
            else -> activity.resources.displayMetrics.widthPixels
        }

        return pxToDp(widthPx).coerceAtLeast(1)
    }

    private fun getShimmerHeightDp(shimmerRoot: View, container: FrameLayout): Int {
        val heightPx = getMeasuredShimmerHeightPx(shimmerRoot, container)
        val heightDp = pxToDp(heightPx)
        return heightDp.coerceAtLeast(50)
    }

    private fun getMeasuredShimmerHeightPx(shimmerRoot: View, container: FrameLayout): Int {
        if (shimmerRoot.height > 0) return shimmerRoot.height
        if (shimmerRoot.measuredHeight > 0) return shimmerRoot.measuredHeight

        val widthPx = when {
            container.width > 0 -> container.width
            container.measuredWidth > 0 -> container.measuredWidth
            else -> activity.resources.displayMetrics.widthPixels
        }

        val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

        shimmerRoot.measure(widthSpec, heightSpec)

        return shimmerRoot.measuredHeight.takeIf { it > 0 } ?: dpToPx(50)
    }

    private fun pxToDp(px: Int): Int {
        return (px / activity.resources.displayMetrics.density).roundToInt()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * activity.resources.displayMetrics.density).roundToInt()
    }

    private fun findRelevantOutMetrics(context: Context): DisplayMetrics {
        val outMetrics = DisplayMetrics()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display = context.display
            display?.getRealMetrics(outMetrics)
        } else {
            @Suppress("DEPRECATION")
            val display =
                (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay

            @Suppress("DEPRECATION")
            display.getMetrics(outMetrics)
        }

        return outMetrics
    }

    private fun findFirstShimmer(view: View): ShimmerFrameLayout? {
        if (view is ShimmerFrameLayout) return view

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val shimmer = findFirstShimmer(view.getChildAt(i))
                if (shimmer != null) return shimmer
            }
        }

        return null
    }

    private fun startViewTracking(view: View) {
        trackedAdView = view
        isViewTrackingActive = true

        visibilityCheckRunnable = object : Runnable {
            override fun run() {
                if (!isViewTrackingActive) return

                if (isViewVisibleOnScreen(view)) {
                    scheduleVisibleForFiveSec()
                } else {
                    mainHandler.postDelayed(this, 300)
                }
            }
        }

        mainHandler.post(visibilityCheckRunnable!!)
    }

    private fun scheduleVisibleForFiveSec() {
        if (visibleForFiveSecRunnable != null) return

        visibleForFiveSecRunnable = Runnable {
            if (isViewTrackingActive && trackedAdView?.let { isViewVisibleOnScreen(it) } == true) {
                lastAdViewedSatisfied = true
                Log.d(TAG, "Ad visible for 5s")
            } else {
                visibilityCheckRunnable?.let { mainHandler.postDelayed(it, 300) }
            }

            visibleForFiveSecRunnable = null
        }

        visibleForFiveSecRunnable?.let { mainHandler.postDelayed(it, 5000) }
    }

    private fun stopViewTracking() {
        isViewTrackingActive = false
        trackedAdView = null

        visibilityCheckRunnable?.let { mainHandler.removeCallbacks(it) }
        visibleForFiveSecRunnable?.let { mainHandler.removeCallbacks(it) }

        visibilityCheckRunnable = null
        visibleForFiveSecRunnable = null
    }

    private fun isViewVisibleOnScreen(view: View): Boolean {
        if (!view.isShown) return false

        val visibleRect = Rect()
        if (!view.getGlobalVisibleRect(visibleRect)) return false

        val visibleArea = visibleRect.width().toLong() * visibleRect.height().toLong()
        val totalArea = view.width.toLong() * view.height.toLong()

        return if (totalArea <= 0) {
            false
        } else {
            (visibleArea * 100 / totalArea) >= 50
        }
    }

    companion object {
        fun showBannerAd(
            activity: Activity,
            container: FrameLayout,
            adId: String,
            adSize: AdSize? = null
        ) {
            val config = BannerAdConfig(
                idAds = adId,
                isFreeSizeAd = false
            )

            val helper = BannerAdHelperNextGen(
                activity = activity,
                lifecycleOwner = activity as LifecycleOwner,
                config = config
            )

            helper.myView = container
            helper.showBannerAdmob()
        }
    }
}
