package com.example.ads.internal

import android.os.SystemClock
import com.example.ads.AdPlacement
import com.example.ads.AdSlotStyle
import com.example.ads.BannerStyle
import com.example.ads.NativeAdDesign
import com.example.ads.compose.nativead.NativeAdTemplateRegistry
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class PreparedSlotController(
    private val scope: CoroutineScope,
    private val banners: BannerAdController,
    private val natives: NativeAdController,
    private val log: AdsLog,
) {

    private val slots = ConcurrentHashMap<String, MutableStateFlow<PreparedSlotAd>>()

    private val requestedStyles = ConcurrentHashMap<String, AdSlotStyle>()

    private val generations = ConcurrentHashMap<String, Int>()

    private val jobs = ConcurrentHashMap<String, Job>()

    fun state(placement: AdPlacement): StateFlow<PreparedSlotAd> = slot(placement).asStateFlow()

    fun prepare(placement: AdPlacement, style: AdSlotStyle) {
        val key = placement.id
        if (style == AdSlotStyle.Hidden) {
            clear(placement)
            return
        }
        val current = slot(placement).value
        val upToDate = requestedStyles[key] == style &&
            (current is PreparedSlotAd.Loading || current.isFresh())
        if (upToDate) return

        val generation = clear(placement)
        requestedStyles[key] = style
        jobs[key] = scope.launch { load(placement, style, generation) }
    }

    fun consume(placement: AdPlacement, shown: PreparedSlotAd, prepareNext: Boolean) {
        val slot = slot(placement)
        val consumed = synchronized(slot) { slot.compareAndSet(shown, PreparedSlotAd.Empty) }
        if (!consumed) return
        shown.release()
        if (!prepareNext) {
            log.d("Prepared slot ${placement.id} consumed, host is finishing")
            return
        }
        val style = requestedStyles[placement.id] ?: return
        log.d("Prepared slot ${placement.id} consumed, preparing next")
        prepare(placement, style)
    }

    fun abandon(placement: AdPlacement) {
        log.d("Prepared slot ${placement.id} abandoned, host is finishing")
        clear(placement)
    }

    fun destroy() {
        slots.keys.toList().forEach { key -> clear(AdPlacement(key)) }
    }

    private fun clear(placement: AdPlacement): Int {
        val key = placement.id
        jobs.remove(key)?.cancel()
        requestedStyles.remove(key)
        val slot = slot(placement)
        val (generation, previous) = synchronized(slot) {
            val next = (generations[key] ?: 0) + 1
            generations[key] = next
            val old = slot.value
            slot.value = PreparedSlotAd.Empty
            next to old
        }
        previous.release()
        return generation
    }

    private suspend fun load(placement: AdPlacement, style: AdSlotStyle, generation: Int) {
        val loaded = when (style) {
            is AdSlotStyle.Banner -> loadBanner(placement, style.style, generation)
            is AdSlotStyle.Native -> loadNative(placement, style.design, generation)
            is AdSlotStyle.BannerWithNativeBackfill ->
                loadBanner(placement, style.style, generation)
                    ?: loadNative(placement, style.backfill, generation)

            AdSlotStyle.Hidden -> null
        }
        val published = publish(placement, generation, loaded ?: PreparedSlotAd.Empty)
        log.d("Prepared slot ${placement.id} ready: ${loaded != null && published}")
    }

    private suspend fun loadBanner(
        placement: AdPlacement,
        style: BannerStyle,
        generation: Int,
    ): PreparedSlotAd? {
        val adSize = banners.adSizeFor(style, 0)
        if (!publish(placement, generation, PreparedSlotAd.Loading(adSize.height))) return null
        val ad = banners.loadOnce(placement, style) ?: return null
        return PreparedSlotAd.Banner(
            ad = ad,
            widthDp = adSize.width,
            heightDp = adSize.height,
            loadedAt = SystemClock.elapsedRealtime(),
        )
    }

    private suspend fun loadNative(
        placement: AdPlacement,
        design: NativeAdDesign,
        generation: Int,
    ): PreparedSlotAd? {
        val loading = PreparedSlotAd.Loading(NativeAdTemplateRegistry.placeholderHeightDp(design))
        if (!publish(placement, generation, loading)) return null
        val ad = natives.loadOnce(placement) ?: return null
        return PreparedSlotAd.Native(ad, design, SystemClock.elapsedRealtime())
    }

    private fun publish(placement: AdPlacement, generation: Int, value: PreparedSlotAd): Boolean {
        val slot = slot(placement)
        val accepted = synchronized(slot) {
            val current = generations[placement.id] == generation
            if (current) slot.value = value
            current
        }
        if (!accepted) value.release()
        return accepted
    }

    private fun slot(placement: AdPlacement): MutableStateFlow<PreparedSlotAd> =
        slots.getOrPut(placement.id) { MutableStateFlow(PreparedSlotAd.Empty) }

    private fun PreparedSlotAd.isFresh(): Boolean {
        val loadedAt = when (this) {
            is PreparedSlotAd.Banner -> loadedAt
            is PreparedSlotAd.Native -> loadedAt
            else -> return false
        }
        return SystemClock.elapsedRealtime() - loadedAt < PREPARED_AD_TTL_MS
    }

    private fun PreparedSlotAd.release() {
        when (this) {
            is PreparedSlotAd.Banner -> runCatching { ad.destroy() }
            is PreparedSlotAd.Native -> runCatching { ad.destroy() }
            else -> Unit
        }
    }

    private companion object {
        const val PREPARED_AD_TTL_MS = 55L * 60L * 1000L
    }
}
