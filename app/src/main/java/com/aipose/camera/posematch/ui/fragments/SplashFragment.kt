package com.aipose.camera.posematch.ui.fragments

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.animation.PathInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import com.aipose.camera.posematch.AppClass
import com.aipose.camera.posematch.MainActivity
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.admob_ads.GoogleMobileAdsConsentManager
import com.aipose.camera.posematch.admob_ads.OpenAppNextGen
import com.aipose.camera.posematch.admob_ads.isSplashOnScreen
import com.aipose.camera.posematch.admob_ads.banner_ad.BannerAdConfig
import com.aipose.camera.posematch.admob_ads.banner_ad.BannerAdHelperNextGen
import com.aipose.camera.posematch.admob_ads.canShowAds
import com.aipose.camera.posematch.admob_ads.inter.loadSplashInterstitialAd
import com.aipose.camera.posematch.admob_ads.inter.showSplashInterstitial
import com.aipose.camera.posematch.admob_ads.native_ad.NativeAdConfig
import com.aipose.camera.posematch.admob_ads.native_ad.NativeAdHelper
import com.aipose.camera.posematch.admob_ads.remote.NATIVE_SPLASH
import com.aipose.camera.posematch.admob_ads.remote.NativeAdStyleManager
import com.aipose.camera.posematch.admob_ads.remote.RemoteConfig
import com.aipose.camera.posematch.admob_ads.simpleCanShowAds
import com.aipose.camera.posematch.databinding.FragmentSplashBinding
import com.aipose.camera.posematch.ui.screens.Routes
import com.aipose.camera.posematch.ui.theme.paletteFor
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.time.Duration.Companion.milliseconds
import androidx.core.content.edit
import com.aipose.camera.posematch.analytics.Analytics

class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val viewModel: MainViewModel by activityViewModels { (requireActivity() as MainActivity).viewModelFactory() }

    private var _binding: FragmentSplashBinding? = null
    private var splashNativeHelper: NativeAdHelper? = null

    private val handler = Handler(Looper.getMainLooper())
    private var startElapsed = 0L

    private var adLoaded = false
    private var shown = false
    private var navigated = false
    private var noAdScheduled = false
    private var adRequestsStarted = false

    // Splash bottom ad (native OR banner): the interstitial must not cover it until it has actually
    // been on screen for BOTTOM_AD_MIN_VISIBLE_MS, so the user really gets to see/interact with it.
    private var splashBottomAdMode = false
    private var bottomAdDisplayedAt = 0L
    private var interHoldStartedAt = 0L
    private var capMs = CAP_NORMAL_MS

    private val capRunnable = Runnable { onCapReached() }
    private val showAdRunnable = Runnable { showAd() }

    // Loading bar. The splash has no fixed duration — it ends when the interstitial is ready, or at
    // NO_AD_MS with no ad, or at the hard cap — so the bar aims at a deadline that is re-targeted as
    // the real finish time becomes known, and only ever reaches 100% on the way out.
    private var progressDeadline = 0L
    private var progressValue = 0f
    private var finishAnimator: ValueAnimator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Never show an App Open ad while the splash is on screen (Google policy) — it runs its own
        // full-screen flow. Cleared once we navigate away.
        isSplashOnScreen = true
        Analytics.screen(Analytics.Screen.SPLASH)

        val binding = FragmentSplashBinding.bind(view).also { _binding = it }
        val palette = paletteFor(view.context)

        view.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(palette.bgTop, palette.bgBottom)
        )

        binding.splashProgress.progressTintList = ColorStateList.valueOf(palette.accent)
        binding.splashProgress.progressBackgroundTintList = ColorStateList.valueOf(palette.glass)

        startAdFlow()
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        // Drop the listener first: cancel() still delivers onAnimationEnd, and running the
        // completion after the view is gone would navigate / show an ad from a dead Fragment.
        finishAnimator?.removeAllListeners()
        finishAnimator?.cancel()
        finishAnimator = null
        isSplashOnScreen = false
        splashNativeHelper = null
        _binding = null
        super.onDestroyView()
    }

    private fun elapsed(): Long = SystemClock.elapsedRealtime() - startElapsed

    // ---- Loading bar ---------------------------------------------------------------------

    private val progressTick = object : Runnable {
        override fun run() {
            renderProgress(progressTarget())
            handler.postDelayed(this, PROGRESS_TICK_MS)
        }
    }

    private fun startProgress() {
        progressDeadline = startElapsed + PROGRESS_ESTIMATE_MS
        handler.post(progressTick)
    }

    /**
     * Re-aim the bar at a newly known finish time. Only ever moves the bar FORWARD: a deadline that
     * slips further out makes it creep instead of rewinding, which would look like a failure.
     */
    private fun setProgressDeadline(absoluteElapsedRealtime: Long) {
        progressDeadline = absoluteElapsedRealtime.coerceAtLeast(startElapsed + 1)
    }

    /**
     * Linear to [PROGRESS_LINEAR_END] across the current estimate, then an exponential creep toward
     * [PROGRESS_CEILING]. Never returns 100 — the bar completes only in [finishProgress], so it can
     * never sit full while the splash is still working.
     */
    private fun progressTarget(): Float {
        val t = elapsed().toFloat()
        val estimate = (progressDeadline - startElapsed).coerceAtLeast(1L).toFloat()
        return if (t <= estimate) {
            PROGRESS_LINEAR_END * (t / estimate)
        } else {
            val over = t - estimate
            PROGRESS_LINEAR_END +
                (PROGRESS_CEILING - PROGRESS_LINEAR_END) * (1f - exp(-over / PROGRESS_OVERRUN_TAU))
        }
    }

    private fun renderProgress(value: Float) {
        // Monotonic — the bar must never step backwards.
        if (value > progressValue) progressValue = value
        val b = _binding ?: return
        b.splashProgress.progress = (progressValue * 10f).toInt().coerceIn(0, 1000)
        b.splashProgressText.text = getString(R.string.splash_progress_percent, progressValue.toInt())
    }

    /**
     * Run the bar out to 100% and then [onComplete]. Uses a ValueAnimator rather than the shared
     * [handler] on purpose: showAd/doNavigate call `removeCallbacksAndMessages(null)`, which would
     * otherwise be able to swallow the completion and strand the splash.
     */
    private fun finishProgress(onComplete: () -> Unit) {
        handler.removeCallbacks(progressTick)
        if (_binding == null || finishAnimator != null) {
            // Nothing to animate. Mark the bar complete so callers that re-enter (doNavigate calls
            // itself once the bar is full) see a finished state instead of looping.
            progressValue = 100f
            onComplete()
            return
        }
        val from = progressValue
        finishAnimator = ValueAnimator.ofFloat(from, 100f).apply {
            duration = PROGRESS_FINISH_MS
            addUpdateListener { renderProgress(it.animatedValue as Float) }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    finishAnimator = null
                    onComplete()
                }
            })
            start()
        }
    }

    private fun startAdFlow() {
        startElapsed = SystemClock.elapsedRealtime()
        startProgress()

        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val firstLaunch = !prefs.getBoolean(KEY_FIRST_DONE, false)
        prefs.edit { putBoolean(KEY_FIRST_DONE, true) }

        // Hard cap: the most we ever wait for an inter ad to load + show.
        capMs = if (firstLaunch) CAP_FIRST_MS else CAP_NORMAL_MS
        handler.postDelayed(capRunnable, capMs)
        Log.d(TAG, "startAdFlow firstLaunch=$firstLaunch cap=${capMs}ms")

        val ctx = requireContext()
        if (!isNetworkAvailable(ctx)) {
            Log.d(TAG, "no network -> no-ad splash")
            scheduleNoAd()
            return
        }

        // Remote Config: cached values are applied synchronously here (no network wait). The
        // background refresh inside applyCachedThenRefresh keeps them current.
        RemoteConfig.applyCachedThenRefresh { }

        if (!ctx.simpleCanShowAds(true)) {
            Log.d(TAG, "ads disabled by remote config -> no-ad splash")
            scheduleNoAd()
            return
        }

        val consentManager = GoogleMobileAdsConsentManager.getInstance(ctx.applicationContext)

        // FAST PATH: UMP already has a usable consent state from a previous session (or none is
        // required here), so we can request ads right now instead of waiting on the consent round
        // trip. gatherConsent still runs below, in parallel, to refresh that state.
        if (consentManager.canRequestAds) {
            Log.d(TAG, "consent already available -> requesting ads immediately")
            beginAdRequests()
        }

        consentManager.gatherConsent(requireActivity()) { canRequestAds ->
            if (!isAdded || shown || navigated) return@gatherConsent
            Log.d(TAG, "gatherConsent complete canRequestAds=$canRequestAds (t=${elapsed()}ms)")
            if (canRequestAds) {
                beginAdRequests()
            } else if (!adRequestsStarted) {
                Log.d(TAG, "consent not granted -> no-ad splash")
                scheduleNoAd()
            }
        }
    }

    /**
     * Kicks off the actual ad requests. Idempotent: both the fast path and the gatherConsent
     * callback may call it, whichever gets there first wins.
     */
    private fun beginAdRequests() {
        if (adRequestsStarted || shown || navigated || !isAdded) return
        adRequestsStarted = true

        val ctx = requireContext()
        AppClass.instance.initializeMobileAdsSdk {
            if (!isAdded || shown || navigated) return@initializeMobileAdsSdk
            if (!ctx.canShowAds(true)) {
                Log.d(TAG, "canShowAds=false after init -> no-ad splash")
                scheduleNoAd()
                return@initializeMobileAdsSdk
            }
            Log.d(TAG, "SDK ready (t=${elapsed()}ms) -> requesting splash ads")

            // Bottom native/banner (decided by Remote Config) — independent of the full-screen
            // splash interstitial decision below, and requested in parallel with it.
            renderSplashBottomAd()

            if (RemoteConfig.isSplashInterEnabled()) {
                Log.d(TAG, "loading splash interstitial…")
                requireActivity().loadSplashInterstitialAd { loaded ->
                    if (!isAdded || shown || navigated) return@loadSplashInterstitialAd
                    if (loaded) {
                        Log.d(TAG, "splash inter loaded (t=${elapsed()}ms)")
                        adLoaded = true
                        // Show as soon as it's loaded — the cap timer is only a MAX wait. If a
                        // bottom ad is on screen, showAd() holds it for its minimum visible time.
                        showAd()
                    } else {
                        Log.d(TAG, "splash inter failed -> no-ad splash")
                        scheduleNoAd()
                    }
                }
            } else {
                // No full-screen splash ad; keep the plain (6s) splash while the bottom
                // native/banner shows, then navigate.
                Log.d(TAG, "splash fullscreen inter disabled -> bottom ad only")
                scheduleNoAd()
            }
        }
    }

    /** Bottom-of-splash native or banner ad, chosen by Remote Config (`nativeSplash`). */
    private fun renderSplashBottomAd() {
        val b = _binding ?: return
        val (contentLayout, shimmer) = NativeAdStyleManager.getAdLayouts(NATIVE_SPLASH)
        when (RemoteConfig.nativeSplashMode) {
            RemoteConfig.NATIVE_MODE -> {
                b.splashAdText.visibility = View.VISIBLE
                b.splashAdHolder.visibility = View.VISIBLE
                splashBottomAdMode = true
                splashNativeHelper = NativeAdHelper(
                    requireActivity(),
                    viewLifecycleOwner,
                    NativeAdConfig(
                        idAds = getString(R.string.NativeSplash),
                        canReloadAds = RemoteConfig.reloadNativeAds,
                        layoutId = contentLayout,
                        shimmerLayout = shimmer,
                        TAG = "SplashNative"
                    )
                ).also {
                    it.nativeContentView = b.splashAdHolder
                    it.onAdDisplayed = { onBottomAdDisplayed("native") }
                    it.showShimmer()
                    it.loadAndShowNativeAd()
                }
            }

            RemoteConfig.BANNER_MODE -> {
                b.splashAdText.visibility = View.VISIBLE
                b.splashAdHolder.visibility = View.VISIBLE
                splashBottomAdMode = true
                BannerAdHelperNextGen(
                    requireActivity(),
                    viewLifecycleOwner,
                    // Splash gets its own banner id (like NativeSplash); every other screen
                    // shares Banner_Ad.
                    BannerAdConfig(getString(R.string.BannerSplash), isFreeSizeAd = true)
                ).apply {
                    myView = b.splashAdHolder
                    onAdDisplayed = { onBottomAdDisplayed("banner") }
                    showBannerUsingShimmerHeight(shimmer)
                }
            }

            else -> {
                // "off" — no bottom ad on splash.
            }
        }
    }

    /** The splash bottom ad is now genuinely on screen — start its minimum-visible window. */
    private fun onBottomAdDisplayed(kind: String) {
        if (bottomAdDisplayedAt != 0L) return
        bottomAdDisplayedAt = SystemClock.elapsedRealtime()
        Log.d(TAG, "splash $kind displayed (t=${elapsed()}ms)")
    }

    /** No ad will show: keep a plain splash for NO_AD_MS total, then navigate. */
    private fun scheduleNoAd() {
        if (noAdScheduled || shown || navigated) return
        noAdScheduled = true
        handler.removeCallbacks(capRunnable)
        val wait = (NO_AD_MS - elapsed()).coerceAtLeast(0)
        // Exact finish time is known now, so the bar can aim straight at it.
        setProgressDeadline(SystemClock.elapsedRealtime() + wait)
        handler.postDelayed({ doNavigate() }, wait)
    }

    private fun onCapReached() {
        if (shown || navigated) return
        if (adLoaded) showAd() else doNavigate()
    }

    /**
     * How much longer the splash bottom ad (native or banner) must stay uncovered before the
     * interstitial may show. Returns 0 when there is no bottom ad, when it has already had its
     * [BOTTOM_AD_MIN_VISIBLE_MS] on screen, or when we've waited [BOTTOM_AD_MAX_WAIT_MS] for it to
     * render and it still hasn't.
     *
     * That last bound matters: previously this polled until the global cap, so a slow bottom ad
     * could sit on a ready interstitial for the entire 15s budget and wreck the show rate.
     */
    private fun bottomAdHoldMs(): Long {
        if (!splashBottomAdMode) return 0L

        if (bottomAdDisplayedAt == 0L) {
            // Bottom ad requested but not rendered yet — give it a bounded window, measured from
            // the moment the interstitial first became ready to show.
            if (interHoldStartedAt == 0L) interHoldStartedAt = SystemClock.elapsedRealtime()
            val waited = SystemClock.elapsedRealtime() - interHoldStartedAt
            return if (waited < BOTTOM_AD_MAX_WAIT_MS) BOTTOM_AD_POLL_MS else 0L
        }

        val visibleFor = SystemClock.elapsedRealtime() - bottomAdDisplayedAt
        return (BOTTOM_AD_MIN_VISIBLE_MS - visibleFor).coerceAtLeast(0L)
    }

    private fun showAd() {
        if (shown || navigated || !isAdded) return

        val hold = bottomAdHoldMs()
        if (hold > 0L) {
            // The interstitial is ready and only the bottom-ad hold is left, so the remaining wait
            // is known — let the bar run out over exactly that window instead of creeping.
            setProgressDeadline(SystemClock.elapsedRealtime() + hold)
            handler.removeCallbacks(showAdRunnable)
            handler.postDelayed(showAdRunnable, hold)
            return
        }

        shown = true
        // Complete the bar before the interstitial covers the splash — otherwise the last thing the
        // user sees is a half-full bar that never finishes.
        finishProgress {
            handler.removeCallbacksAndMessages(null)
            Log.d(TAG, "showing splash interstitial (t=${elapsed()}ms)")
            requireActivity().showSplashInterstitial(
                closeListener = { doNavigate() },
                failListener = { doNavigate() }
            )
        }
    }

    private fun doNavigate() {
        if (navigated || !isAdded) return
        // Navigating without an interstitial: the splash is still what the user is looking at, so
        // run the bar out first. After the ad has shown, the splash is already covered — go now.
        if (!shown && finishAnimator == null && progressValue < 100f) {
            finishProgress { doNavigate() }
            return
        }
        navigated = true
        handler.removeCallbacksAndMessages(null)

        // Leaving splash: allow App Open again, and initialize it for future background->foreground
        // returns (cold start is covered by the splash itself, so this only fires on later resumes).
        isSplashOnScreen = false
        val ctx = context
        if (ctx != null && RemoteConfig.openAd && ctx.canShowAds(RemoteConfig.openAd)) {
            OpenAppNextGen.initialize(AppClass.instance, getString(R.string.AppOpenResume))
        }

        // Only navigate while at least STARTED (avoids FragmentManager dropping it if backgrounded).
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.withStarted {
                if (isAdded) navigateToRoute(Routes.MAIN_CONTAINER)
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private companion object {
        const val TAG = "SplashAds"
        const val PREFS = "posematch_splash"
        const val KEY_FIRST_DONE = "first_launch_done"

        const val CAP_FIRST_MS = 15000L  // first launch: MAX wait for the ad (consent + init are slow)
        const val CAP_NORMAL_MS = 12000L // subsequent launches: MAX wait for the ad
        const val NO_AD_MS = 6000L       // plain splash when no ad will show

        // Minimum time the splash bottom ad (native or banner) stays uncovered before the
        // interstitial may show.
        const val BOTTOM_AD_MIN_VISIBLE_MS = 3000L

        // Max extra time a READY interstitial waits for a slow bottom ad to render. Prevents a
        // slow network from letting the bottom ad consume the whole splash budget.
        const val BOTTOM_AD_MAX_WAIT_MS = 4000L
        const val BOTTOM_AD_POLL_MS = 200L

        // ---- Loading bar ----
        const val PROGRESS_TICK_MS = 40L         // ~25fps; the bar moves slowly, this is plenty
        // Opening guess at how long the splash will take. Deliberately the no-ad duration rather
        // than the 12–15s cap: most launches finish near here, and aiming at the cap would leave
        // the bar at ~25% when a fast interstitial arrives, then jump.
        const val PROGRESS_ESTIMATE_MS = 6000L
        const val PROGRESS_LINEAR_END = 92f      // linear portion ends here
        const val PROGRESS_CEILING = 99f         // creep asymptote when the splash overruns
        const val PROGRESS_OVERRUN_TAU = 3000f   // creep time constant, ms
        const val PROGRESS_FINISH_MS = 260L      // run-out to 100% on the way off the splash
    }
}
