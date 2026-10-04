package com.aipose.camera.posematch.admob_ads

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.aipose.camera.posematch.AppClass
import com.aipose.camera.posematch.R
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.aipose.camera.posematch.admob_ads.remote.RemoteConfig
import java.util.Date
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import com.aipose.camera.posematch.analytics.Analytics

var isAppOpenEnable = true

// Google policy guards for App Open:
//  - isSplashOnScreen: never show App Open while the splash is on screen (splash runs its own
//    full-screen flow). Set true by SplashFragment, cleared when it leaves.
//  - skipNextAppOpen: skip exactly one background->foreground App Open after the user leaves the app
//    via an in-app action (Settings: Share / Privacy Policy / Rate / Feedback). Consumed once.
var isSplashOnScreen = false
var skipNextAppOpen = false

@SuppressLint("StaticFieldLeak")
object OpenAppNextGen : Application.ActivityLifecycleCallbacks, LifecycleEventObserver {
    private const val TAG = "TESTINGOpenApp"

    private val mainHandler = Handler(Looper.getMainLooper())
    private val loadCallbacks = CopyOnWriteArrayList<(Boolean) -> Unit>()

    private var appOpenAd: AppOpenAd? = null
    private var currentActivity: Activity? = null
    private var isShowingAd = false
    private var isLoadingAd = false
    private var loadTime: Long = 0
    private var loadingDialog: Dialog? = null
    private var myApplication: AppClass? = null
    private var hasEnteredBackground = false

    fun initialize(application: AppClass, appOpenId: String) {
        Log.d(TAG, "initialize: appOpenId=$appOpenId")
        ensureInitialized(application)
        loadAppOpenAd(appOpenId)
    }

    fun loadSplashAppOpen(
        application: AppClass,
        appOpenId: String,
        onResult: (Boolean) -> Unit
    ) {
        Log.d(TAG, "loadSplashAppOpen")
        ensureInitialized(application)
        loadAppOpenAd(appOpenId, onResult)
    }

    fun isSplashAppOpenAvailable(): Boolean {
        return isAdAvailable()
    }

    fun forceShowSplashAppOpen(
        activity: Activity,
        onShown: (() -> Unit)? = null,
        onComplete: () -> Unit
    ) {
        Log.d(TAG, "forceShowSplashAppOpen")
        currentActivity = activity
        ensureInitialized(AppClass.instance)

        val completionDispatched = AtomicBoolean(false)
        val shownDispatched = AtomicBoolean(false)

        fun completeOnce() {
            if (completionDispatched.compareAndSet(false, true)) {
                onComplete.invoke()
            }
        }

        fun shownOnce() {
            if (shownDispatched.compareAndSet(false, true)) {
                onShown?.invoke()
            }
        }

        if (activity.isFinishing || activity.isDestroyed) {
            Log.d(TAG, "forceShowSplashAppOpen: activity is finishing or destroyed")
            completeOnce()
            return
        }

        if (isShowingAd || FullScreenAdGate.isAnyActive) {
            Log.d(TAG, "forceShowSplashAppOpen: app open already showing or another full-screen ad owns the screen")
            completeOnce()
            return
        }

        // The user left the app through an in-app action (Settings: share / rate / privacy /
        // feedback, gallery picker, ...) — returning from that must not be treated as a fresh app
        // open. showAdIfAvailable() honours this too; checking here as well means no show path can
        // bypass it.
        if (skipNextAppOpen) {
            skipNextAppOpen = false
            Log.d(TAG, "forceShowSplashAppOpen: skipped once (external navigation)")
            completeOnce()
            return
        }

        if (!RemoteConfig.openAd) {
            Log.d(TAG, "forceShowSplashAppOpen: app open disabled remotely")
            completeOnce()
            return
        }

        if (!GoogleMobileAdsConsentManager.getInstance(activity).canRequestAds) {
            Log.d(TAG, "forceShowSplashAppOpen: consent not granted")
            completeOnce()
            return
        }

        if (!activity.canShowAds(RemoteConfig.openAd)) {
            Log.d(TAG, "forceShowSplashAppOpen: ads cannot show right now")
            loadAppOpenAd(activity.getString(R.string.AppOpenResume))
            completeOnce()
            return
        }

        if (!activity.ensureMobileAdsSdkInitialized(TAG, "forceShowSplashAppOpen")) {
            Log.d(TAG, "forceShowSplashAppOpen: SDK not initialized")
            completeOnce()
            return
        }

        val ad = appOpenAd.takeIf { isAdAvailable() }
        if (ad == null) {
            Log.d(TAG, "forceShowSplashAppOpen: Ad not available")
            loadAppOpenAd(activity.getString(R.string.AppOpenResume))
            completeOnce()
            return
        }

        if (!FullScreenAdGate.beginAppOpen()) {
            Log.d(TAG, "forceShowSplashAppOpen: blocked by gate")
            completeOnce()
            return
        }

        showLoadingDialog(activity)
        isShowingAd = true
        appOpenAd = null

        ad.adEventCallback = object : AppOpenAdEventCallback {
            override fun onAdDismissedFullScreenContent() {
                activity.runOnUiThread {
                    Log.d(TAG, "forceShowSplashAppOpen: onAdDismissed")
                    Analytics.ad(Analytics.AdAction.DISMISSED, "app_open", "splash")
                    hideLoadingDialog(activity)
                    isShowingAd = false
                    FullScreenAdGate.endAppOpen()
                    loadAppOpenAd(activity.getString(R.string.AppOpenResume))
                    completeOnce()
                }
            }

            override fun onAdFailedToShowFullScreenContent(error: FullScreenContentError) {
                activity.runOnUiThread {
                    Log.d(
                        TAG,
                        "forceShowSplashAppOpen: onAdFailedToShow=${error.message}"
                    )
                    hideLoadingDialog(activity)
                    isShowingAd = false
                    FullScreenAdGate.endAppOpen()
                    loadAppOpenAd(activity.getString(R.string.AppOpenResume))
                    completeOnce()
                }
            }

            override fun onAdShowedFullScreenContent() {
                activity.runOnUiThread {
                    Log.d(TAG, "forceShowSplashAppOpen: onAdShowed")
                    Analytics.ad(Analytics.AdAction.SHOWN, "app_open", "splash")
                    hideLoadingDialog(activity)
                    shownOnce()
                }
            }

            override fun onAdImpression() {
                Log.d(TAG, "forceShowSplashAppOpen: onAdImpression")
            }

            override fun onAdClicked() {
                Log.d(TAG, "forceShowSplashAppOpen: onAdClicked")
            }
        }

        try {
            ad.show(activity)
        } catch (exception: Exception) {
            Log.e(TAG, "forceShowSplashAppOpen: show exception", exception)
            hideLoadingDialog(activity)
            isShowingAd = false
            FullScreenAdGate.endAppOpen()
            loadAppOpenAd(activity.getString(R.string.AppOpenResume))
            completeOnce()
        }
    }

    fun forceShowAppOpen(activity: Activity, onShowAdCompleteListener: (() -> Unit)? = null) {
        forceShowSplashAppOpen(
            activity = activity,
            onComplete = { onShowAdCompleteListener?.invoke() }
        )
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_STOP -> hasEnteredBackground = true
            Lifecycle.Event.ON_START -> {
                if (!hasEnteredBackground) return
                hasEnteredBackground = false
                currentActivity?.let { activity ->
                    if (activity !is SplashScreen) {
                        showAdIfAvailable()
                    }
                }
            }

            else -> Unit
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }

    private fun ensureInitialized(application: AppClass) {
        if (myApplication != null) {
            return
        }

        myApplication = application
        myApplication?.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    private fun loadAppOpenAd(
        appOpenId: String,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        val context = myApplication ?: run {
            dispatchLoadResult(onResult, false)
            return
        }

        if (isAdAvailable()) {
            Log.d(TAG, "loadAppOpenAd: ad already available")
            dispatchLoadResult(onResult, true)
            return
        }

        onResult?.let(loadCallbacks::add)

        if (isLoadingAd) {
            Log.d(TAG, "loadAppOpenAd: load already running")
            return
        }

        if (!RemoteConfig.openAd) {
            Log.d(TAG, "loadAppOpenAd: app open disabled remotely")
            notifyLoadCallbacks(false)
            return
        }

        if (!GoogleMobileAdsConsentManager.getInstance(context).canRequestAds) {
            Log.d(TAG, "loadAppOpenAd: consent not granted")
            notifyLoadCallbacks(false)
            return
        }

        if (!context.canShowAds(RemoteConfig.openAd)) {
            Log.d(TAG, "loadAppOpenAd: ads cannot show right now")
            notifyLoadCallbacks(false)
            return
        }

        if (!context.ensureMobileAdsSdkInitialized(TAG, "loadAppOpenAd")) {
            Log.d(TAG, "loadAppOpenAd: SDK not initialized")
            notifyLoadCallbacks(false)
            return
        }

        isLoadingAd = true
        Log.d(TAG, "loadAppOpenAd: Loading App Open Ad: $appOpenId")
        Analytics.ad(Analytics.AdAction.REQUESTED, "app_open", "resume")

        val request = AdRequest.Builder(appOpenId).build()
        AppOpenAd.load(
            request,
            object : AdLoadCallback<AppOpenAd> {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "loadAppOpenAd: load success")
                    Analytics.ad(Analytics.AdAction.LOADED, "app_open", "resume")
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    notifyLoadCallbacks(true)
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "loadAppOpenAd: load failed=${adError.message}")
                    Analytics.ad(Analytics.AdAction.FAILED, "app_open", "resume", adError.message)
                    isLoadingAd = false
                    appOpenAd = null
                    notifyLoadCallbacks(false)
                }
            }
        )
    }

    private fun showAdIfAvailable() {
        val activity = currentActivity ?: return
        Log.d(
            TAG,
            "showAdIfAvailable: isAppOpenEnable=$isAppOpenEnable, isAnyActive=${FullScreenAdGate.isAnyActive}, configOpenAd=${RemoteConfig.openAd}"
        )

        if (isSplashOnScreen) {
            Log.d(TAG, "showAdIfAvailable: skipped (splash on screen)")
            return
        }

        if (skipNextAppOpen) {
            skipNextAppOpen = false
            Log.d(TAG, "showAdIfAvailable: skipped once (external navigation: share/privacy/etc.)")
            return
        }

        if (!isAppOpenEnable || activity is SplashScreen || FullScreenAdGate.isAnyActive) {
            Log.d(TAG, "showAdIfAvailable: skipped by current state")
            return
        }

        if (isAdAvailable()) {
            showLoadedAppOpen(activity)
        } else {
            Log.d(TAG, "showAdIfAvailable: Ad not available, fetching...")
            loadAppOpenAd(activity.getString(R.string.AppOpenResume))
        }
    }

    private fun showLoadedAppOpen(activity: Activity): Boolean {
        if (activity.isFinishing || activity.isDestroyed) {
            Log.d(TAG, "showLoadedAppOpen: activity is finishing or destroyed")
            return false
        }

        if (isShowingAd) {
            Log.d(TAG, "showLoadedAppOpen: already showing")
            return false
        }

        if (!RemoteConfig.openAd || !GoogleMobileAdsConsentManager.getInstance(activity).canRequestAds) {
            Log.d(TAG, "showLoadedAppOpen: disabled by config or consent")
            return false
        }

        if (!activity.canShowAds(RemoteConfig.openAd)) {
            Log.d(TAG, "showLoadedAppOpen: ads cannot show")
            return false
        }

        if (!activity.ensureMobileAdsSdkInitialized(TAG, "showLoadedAppOpen")) {
            Log.d(TAG, "showLoadedAppOpen: SDK not initialized")
            return false
        }

        val ad = appOpenAd.takeIf { isAdAvailable() } ?: run {
            Log.d(TAG, "showLoadedAppOpen: ad no longer available")
            return false
        }

        if (!FullScreenAdGate.beginAppOpen()) {
            Log.d(TAG, "showLoadedAppOpen: blocked by gate")
            return false
        }

        showLoadingDialog(activity)
        isShowingAd = true
        appOpenAd = null

        ad.adEventCallback = object : AppOpenAdEventCallback {
            override fun onAdDismissedFullScreenContent() {
                activity.runOnUiThread {
                    Log.d(TAG, "showLoadedAppOpen: onAdDismissed")
                    Analytics.ad(Analytics.AdAction.DISMISSED, "app_open", "resume")
                    hideLoadingDialog(activity)
                    isShowingAd = false
                    FullScreenAdGate.endAppOpen()
                    loadAppOpenAd(activity.getString(R.string.AppOpenResume))
                }
            }

            override fun onAdFailedToShowFullScreenContent(error: FullScreenContentError) {
                activity.runOnUiThread {
                    Log.d(TAG, "showLoadedAppOpen: onAdFailedToShow=${error.message}")
                    hideLoadingDialog(activity)
                    isShowingAd = false
                    FullScreenAdGate.endAppOpen()
                    loadAppOpenAd(activity.getString(R.string.AppOpenResume))
                }
            }

            override fun onAdShowedFullScreenContent() {
                activity.runOnUiThread {
                    Log.d(TAG, "showLoadedAppOpen: onAdShowed")
                    Analytics.ad(Analytics.AdAction.SHOWN, "app_open", "resume")
                    hideLoadingDialog(activity)
                }
            }

            override fun onAdImpression() {
                Log.d(TAG, "showLoadedAppOpen: onAdImpression")
            }

            override fun onAdClicked() {
                Log.d(TAG, "showLoadedAppOpen: onAdClicked")
                Analytics.ad(Analytics.AdAction.CLICKED, "app_open", "resume")
            }
        }

        return try {
            ad.show(activity)
            true
        } catch (exception: Exception) {
            Log.e(TAG, "showLoadedAppOpen: show exception", exception)
            hideLoadingDialog(activity)
            isShowingAd = false
            FullScreenAdGate.endAppOpen()
            loadAppOpenAd(activity.getString(R.string.AppOpenResume))
            false
        }
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && (Date().time - loadTime) < 3600000 * 4
    }

    private fun notifyLoadCallbacks(loaded: Boolean) {
        val callbacks = loadCallbacks.toList()
        loadCallbacks.clear()
        callbacks.forEach { callback ->
            dispatchLoadResult(callback, loaded)
        }
    }

    private fun dispatchLoadResult(
        callback: ((Boolean) -> Unit)?,
        loaded: Boolean
    ) {
        callback ?: return
        mainHandler.post {
            callback.invoke(loaded)
        }
    }

    private fun showLoadingDialog(activity: Activity) {
        activity.runOnUiThread {
            try {
                if (activity.isFinishing || activity.isDestroyed) {
                    return@runOnUiThread
                }

                hideLoadingDialog(activity)
                loadingDialog = Dialog(activity).apply {
                    requestWindowFeature(Window.FEATURE_NO_TITLE)
                    setContentView(R.layout.dialog_resume_loading)
                    setCancelable(false)
                    window?.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                    )
                    show()
                }
            } catch (exception: Exception) {
                Log.e(TAG, "showLoadingDialog: error", exception)
            }
        }
    }

    private fun hideLoadingDialog(activity: Activity? = currentActivity) {
        activity?.runOnUiThread {
            try {
                loadingDialog?.let { dialog ->
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }
            } catch (exception: Exception) {
                Log.e(TAG, "hideLoadingDialog: error", exception)
            } finally {
                loadingDialog = null
            }
        }
    }
}
