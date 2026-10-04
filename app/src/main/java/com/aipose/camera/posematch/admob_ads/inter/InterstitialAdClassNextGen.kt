package com.aipose.camera.posematch.admob_ads.inter

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.aipose.camera.posematch.R
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.aipose.camera.posematch.admob_ads.GoogleMobileAdsConsentManager
import com.aipose.camera.posematch.admob_ads.FullScreenAdGate
import com.aipose.camera.posematch.admob_ads.canShowAds
import com.aipose.camera.posematch.admob_ads.ensureMobileAdsSdkInitialized
import com.aipose.camera.posematch.admob_ads.remote.RemoteConfig
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import com.aipose.camera.posematch.analytics.Analytics

open class InterstitialAdClassNextGen {
    var mInterstitialAd: InterstitialAd? = null
    private var splashInterstitialAd: InterstitialAd? = null
    private val logTag = "interstitialAdFlow"
    private val isAdLoading = AtomicBoolean(false)
    private val isSplashAdLoading = AtomicBoolean(false)
    private val isAdShowing = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val splashLoadCallbacks = CopyOnWriteArrayList<(Boolean) -> Unit>()

    private var loadingDialog: Dialog? = null
    private var activeShowRequestId = 0L
    private var hasDialogBeenShownForRequest = false
    private var hasAdBeenShownForRequest = false
    private var pendingShowRunnable: Runnable? = null
    /** Which slot the CURRENT show request belongs to, for analytics: "splash" or "in_app". */
    private var currentPlacement = "in_app"

    companion object {
        // Global "loading ad…" dialog duration shown before every interstitial (splash, home, etc.).
        private const val INTERSTITIAL_DIALOG_DURATION_MS = 2000L
        var failCounter = 0

        @Volatile
        private var instance: InterstitialAdClassNextGen? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: InterstitialAdClassNextGen().also { instance = it }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    private fun canRequestAdByConsent(context: Context): Boolean {
        return GoogleMobileAdsConsentManager.getInstance(context).canRequestAds
    }

    fun loadSimpleInterstitialAd(context: Context, adId: String) {
        showInterstitialAdLog("Loading Ad ...")

        // Single choke point for every InterHome request — the Home preload, the failure retry and
        // the post-dismiss reload all land here. Without this, an ad is requested whenever the
        // master switch is on, even with all placement keys off, so it can never be shown.
        if (!RemoteConfig.anyInAppInterEnabled()) {
            showInterstitialAdLog("No in-app interstitial placement enabled - skipping load")
            return
        }

        if (isAdLoading.get()) {
            showInterstitialAdLog("Load already in progress - skipping new request")
            return
        }

        if (canRequestAdByConsent(context)) {
            mInterstitialAd?.let {
                return
            } ?: run {
                showInterstitialAdLog("Loading Ad 1...")
                Analytics.ad(Analytics.AdAction.REQUESTED, "interstitial", "in_app")
                isAdLoading.set(true)

                if (!context.ensureMobileAdsSdkInitialized(logTag, "loadSimpleInterstitialAd")) {
                    isAdLoading.set(false)
                    return
                }

                val adRequest = AdRequest.Builder(adId).build()
                InterstitialAd.load(
                    adRequest,
                    object : AdLoadCallback<InterstitialAd> {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            isAdLoading.set(false)
                            mInterstitialAd = null
                            if (failCounter < 2) {
                                failCounter++
                                loadSimpleInterstitialAd(context, context.getString(R.string.InterHome))
                            } else {
                                failCounter = 0
                            }
                            showInterstitialAdLog("Ad failed to load because ${error.message}")
                            Analytics.ad(Analytics.AdAction.FAILED, "interstitial", "in_app", error.message)
                        }

                        override fun onAdLoaded(ad: InterstitialAd) {
                            isAdLoading.set(false)
                            mInterstitialAd = ad
                            showInterstitialAdLog("Ad successfully loaded")
                    Analytics.ad(Analytics.AdAction.LOADED, "interstitial", "in_app")
                        }
                    })
            }
        }
    }

    fun showSimpleInterstitialAdNew(
        activity: Activity,
        closeListener: (() -> Unit)? = null,
        failListener: (() -> Unit)? = null,
        showListener: (() -> Unit)? = null,
        onNext: (() -> Unit)? = null,
    ) {
        showInterstitialAdLog("Showing Ad ...")
        currentPlacement = "in_app"
        showInterstitial(
            activity = activity,
            adProvider = { mInterstitialAd },
            clearAd = { mInterstitialAd = null },
            closeListener = closeListener,
            failListener = failListener,
            showListener = showListener,
            onNext = onNext,
            reloadAfterDismiss = true
        )
    }

    fun loadSplashInterstitialAd(context: Context, onResult: (Boolean) -> Unit) {
        showInterstitialAdLog("Loading splash interstitial ...")
        Analytics.ad(Analytics.AdAction.REQUESTED, "interstitial", "splash")

        if (!context.canShowAds(RemoteConfig.isSplashInterEnabled())) {
            showInterstitialAdLog("Splash interstitial blocked by ads eligibility checks")
            dispatchSplashLoadResult(onResult, false)
            return
        }

        if (!canRequestAdByConsent(context)) {
            showInterstitialAdLog("Splash interstitial blocked by consent")
            dispatchSplashLoadResult(onResult, false)
            return
        }

        if (!context.ensureMobileAdsSdkInitialized(logTag, "loadSplashInterstitialAd")) {
            showInterstitialAdLog("Splash interstitial blocked because SDK is not initialized")
            dispatchSplashLoadResult(onResult, false)
            return
        }

        splashInterstitialAd?.let {
            showInterstitialAdLog("Splash interstitial already available")
            dispatchSplashLoadResult(onResult, true)
            return
        }

        splashLoadCallbacks.add(onResult)

        if (!isSplashAdLoading.compareAndSet(false, true)) {
            showInterstitialAdLog("Splash interstitial load already in progress")
            return
        }

        val adRequest = AdRequest.Builder(context.getString(R.string.InterSplash)).build()
        InterstitialAd.load(
            adRequest,
            object : AdLoadCallback<InterstitialAd> {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isSplashAdLoading.set(false)
                    splashInterstitialAd = null
                    showInterstitialAdLog("Splash interstitial failed: ${error.message}")
                    Analytics.ad(Analytics.AdAction.FAILED, "interstitial", "splash", error.message)
                    notifySplashLoadCallbacks(false)
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    isSplashAdLoading.set(false)
                    splashInterstitialAd = ad
                    showInterstitialAdLog("Splash interstitial loaded")
                    Analytics.ad(Analytics.AdAction.LOADED, "interstitial", "splash")
                    notifySplashLoadCallbacks(true)
                }
            }
        )
    }

    fun showSplashInter(
        activity: Activity,
        closeListener: (() -> Unit)? = null,
        failListener: (() -> Unit)? = null,
        showListener: (() -> Unit)? = null,
        onNext: (() -> Unit)? = null,
    ) {
        showInterstitialAdLog("Showing Ad ...")
        currentPlacement = "splash"
        showInterstitial(
            activity = activity,
            adProvider = { splashInterstitialAd },
            clearAd = { splashInterstitialAd = null },
            closeListener = closeListener,
            failListener = failListener,
            showListener = showListener,
            onNext = onNext,
            reloadAfterDismiss = false
        )
    }

    private fun showInterstitial(
        activity: Activity,
        adProvider: () -> InterstitialAd?,
        clearAd: () -> Unit,
        closeListener: (() -> Unit)?,
        failListener: (() -> Unit)?,
        showListener: (() -> Unit)?,
        onNext: (() -> Unit)?,
        reloadAfterDismiss: Boolean
    ) {
        if (!isNetworkAvailable(activity) || !canRequestAdByConsent(activity)) {
            showInterstitialAdLog("Ad is null or consent not granted, not showing ...")
            failListener?.invoke()
            return
        }

        if (!activity.ensureMobileAdsSdkInitialized(logTag, "showInterstitial")) {
            failListener?.invoke()
            return
        }

        if (!isAdShowing.compareAndSet(false, true)) {
            showInterstitialAdLog("Show already in progress - skipping duplicate request")
            return
        }

        val ad = adProvider()
        if (ad == null) {
            isAdShowing.set(false)
            showInterstitialAdLog("Ad is null , not showing ...")
            // Only the in-app flow refills from here. The splash path (reloadAfterDismiss = false)
            // used to fall through and request InterHome — the wrong ad unit entirely.
            if (reloadAfterDismiss) {
                loadSimpleInterstitialAd(activity, activity.getString(R.string.InterHome))
            }
            failListener?.invoke()
            return
        }

        // Claim the full-screen slot for the ENTIRE flow (bridge dialog included) so an App Open
        // ad can never slip in and end up under/over this interstitial.
        if (!FullScreenAdGate.beginInterstitial()) {
            isAdShowing.set(false)
            showInterstitialAdLog("Another full-screen ad owns the screen - not showing")
            failListener?.invoke()
            return
        }

        val requestId = startNewShowRequest()
        showLoadingDialogOnce(activity, requestId)
        showInterstitialAdLog("Ad is not null, calling show and setting listener ...")

        ad.adEventCallback = object : InterstitialAdEventCallback {
            override fun onAdDismissedFullScreenContent() {
                activity.runOnUiThread {
                    showInterstitialAdLog("Ad closed by user")
                    Analytics.ad(Analytics.AdAction.DISMISSED, "interstitial", currentPlacement)
                    clearAd()
                    finishShowRequest(requestId)
                    closeListener?.invoke()
                    mainHandler.postDelayed({
                        if (reloadAfterDismiss) {
                            loadSimpleInterstitialAd(activity, activity.getString(R.string.InterHome))
                        }
                    }, 1000)
                }
            }

            override fun onAdFailedToShowFullScreenContent(error: FullScreenContentError) {
                activity.runOnUiThread {
                    showInterstitialAdLog("Ad failed to show: ${error.message}")
                    clearAd()
                    finishShowRequest(requestId)
                    failListener?.invoke()
                    mainHandler.postDelayed({
                        if (reloadAfterDismiss) {
                            loadSimpleInterstitialAd(activity, activity.getString(R.string.InterHome))
                        }
                    }, 1000)
                }
            }

            override fun onAdShowedFullScreenContent() {
                activity.runOnUiThread {
                    showInterstitialAdLog("Ad successfully showed")
                    Analytics.ad(Analytics.AdAction.SHOWN, "interstitial", currentPlacement)
                    dismissLoadingDialog()
                    onNext?.invoke()
                    showListener?.invoke()
                }
            }
        }

        scheduleAdShow(activity, ad, requestId, failListener)
    }

    private fun startNewShowRequest(): Long {
        activeShowRequestId = System.nanoTime()
        hasDialogBeenShownForRequest = false
        hasAdBeenShownForRequest = false
        pendingShowRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingShowRunnable = null
        return activeShowRequestId
    }

    private fun showLoadingDialogOnce(activity: Activity, requestId: Long) {
        activity.runOnUiThread {
            if (!isActiveRequest(requestId) || hasDialogBeenShownForRequest) {
                return@runOnUiThread
            }

            if (activity.isFinishing || activity.isDestroyed) {
                finishShowRequest(requestId)
                return@runOnUiThread
            }

            val currentDialog = loadingDialog
            if (currentDialog?.isShowing == true && currentDialog.context === activity) {
                hasDialogBeenShownForRequest = true
                return@runOnUiThread
            }

            dismissLoadingDialog()
            loadingDialog = Dialog(activity, R.style.FullScreenDialog).apply {
                setContentView(R.layout.dialog_resume_loading)
                setCancelable(false)

                show()
            }
            hasDialogBeenShownForRequest = true
        }
    }

    private fun scheduleAdShow(
        activity: Activity,
        ad: InterstitialAd,
        requestId: Long,
        failListener: (() -> Unit)?
    ) {
        val runnable = Runnable {
            activity.runOnUiThread {
                if (!isActiveRequest(requestId)) {
                    return@runOnUiThread
                }

                if (activity.isFinishing || activity.isDestroyed) {
                    finishShowRequest(requestId)
                    return@runOnUiThread
                }

                if (!ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    finishShowRequest(requestId)
                    failListener?.invoke()
                    return@runOnUiThread
                }

                if (hasAdBeenShownForRequest) {
                    showInterstitialAdLog("Ad show already triggered for this request")
                    return@runOnUiThread
                }

                hasAdBeenShownForRequest = true
                dismissLoadingDialog()
                ad.show(activity)
            }
        }

        pendingShowRunnable = runnable
        mainHandler.postDelayed(runnable, INTERSTITIAL_DIALOG_DURATION_MS)
    }

    private fun isActiveRequest(requestId: Long): Boolean {
        return isAdShowing.get() && activeShowRequestId == requestId
    }

    private fun finishShowRequest(requestId: Long) {
        if (activeShowRequestId != requestId) {
            return
        }

        pendingShowRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingShowRunnable = null
        dismissLoadingDialog()
        hasDialogBeenShownForRequest = false
        hasAdBeenShownForRequest = false
        isAdShowing.set(false)
        // Single funnel for dismiss / fail / timeout / activity-gone — release the shared slot here
        // so App Open becomes eligible again (after its cooldown).
        FullScreenAdGate.endInterstitial()
    }

    fun isSplashInterstitialAvailable(): Boolean {
        return splashInterstitialAd != null
    }

    private fun notifySplashLoadCallbacks(loaded: Boolean) {
        val callbacks = splashLoadCallbacks.toList()
        splashLoadCallbacks.clear()
        callbacks.forEach { callback ->
            dispatchSplashLoadResult(callback, loaded)
        }
    }

    private fun dispatchSplashLoadResult(
        callback: (Boolean) -> Unit,
        loaded: Boolean
    ) {
        mainHandler.post {
            callback.invoke(loaded)
        }
    }

    private fun showInterstitialAdLog(msg: String) {
        Log.d(logTag, msg)
    }

    private fun dismissLoadingDialog() {
        try {
            loadingDialog?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }
        } catch (_: Exception) {
        } finally {
            loadingDialog = null
        }
    }
}
