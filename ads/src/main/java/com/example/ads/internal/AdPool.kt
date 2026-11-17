package com.example.ads.internal

import android.os.SystemClock
import java.util.ArrayDeque
import java.util.IdentityHashMap
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

internal class AdPool<T : Any>(
    private val scope: CoroutineScope,
    private val destroy: (T) -> Unit,
    private val now: () -> Long = SystemClock::elapsedRealtime,
) {

    private val lock = Any()
    private val pooled = mutableMapOf<String, ArrayDeque<Entry<T>>>()
    private val entries = IdentityHashMap<T, Entry<T>>()
    private val loads = ConcurrentHashMap<String, Deferred<Boolean>>()

    suspend fun acquire(key: String, load: suspend () -> T?): T? {
        take(key)?.let { return it }
        if (!sharedLoad(key, load).await()) return null
        return take(key)
    }

    fun prefetch(key: String, load: suspend () -> T?) {
        if (hasAvailable(key) || loads[key]?.isActive == true) return
        scope.launch { sharedLoad(key, load).await() }
    }

    fun markImpressed(ad: T) {
        synchronized(lock) { entries[ad]?.impressed = true }
    }

    fun loadedAt(ad: T): Long? = synchronized(lock) { entries[ad]?.loadedAt }

    fun recycle(ad: T) {
        val destroyAd = synchronized(lock) {
            val entry = entries[ad] ?: return
            if (entry.impressed || entry.isExpired(now())) {
                entries.remove(ad)
                true
            } else {
                val queue = pooled.getOrPut(entry.key) { ArrayDeque() }
                if (queue.any { pooledEntry -> pooledEntry.ad === ad }) {
                    false
                } else if (queue.size >= MAX_POOLED_ADS_PER_KEY) {
                    entries.remove(ad)
                    true
                } else {
                    queue.addLast(entry)
                    false
                }
            }
        }
        if (destroyAd) destroySafely(ad)
    }

    fun clear() {
        val ads = synchronized(lock) {
            val current = entries.keys.toList()
            entries.clear()
            pooled.clear()
            current
        }
        loads.values.forEach { load -> load.cancel() }
        loads.clear()
        ads.forEach(::destroySafely)
    }

    private fun sharedLoad(key: String, load: suspend () -> T?): Deferred<Boolean> {
        loads[key]?.let { return it }
        val deferred = scope.async(start = CoroutineStart.LAZY) {
            val ad = runCatching { load() }.getOrNull() ?: return@async false
            addLoaded(key, ad)
        }
        val active = loads.putIfAbsent(key, deferred)
        if (active != null) {
            deferred.cancel()
            return active
        }
        deferred.invokeOnCompletion { loads.remove(key, deferred) }
        deferred.start()
        return deferred
    }

    private fun addLoaded(key: String, ad: T): Boolean {
        val accepted = synchronized(lock) {
            val queue = pooled.getOrPut(key) { ArrayDeque() }
            if (queue.size >= MAX_POOLED_ADS_PER_KEY) {
                false
            } else {
                val entry = Entry(key, ad, now())
                entries[ad] = entry
                queue.addLast(entry)
                true
            }
        }
        if (!accepted) destroySafely(ad)
        return accepted
    }

    private fun take(key: String): T? {
        val expired = mutableListOf<T>()
        val result = synchronized(lock) {
            val queue = pooled[key] ?: return@synchronized null
            var available: T? = null
            while (queue.isNotEmpty() && available == null) {
                val entry = queue.removeFirst()
                if (entry.impressed || entry.isExpired(now())) {
                    entries.remove(entry.ad)
                    expired += entry.ad
                } else {
                    available = entry.ad
                }
            }
            if (queue.isEmpty()) pooled.remove(key)
            available
        }
        expired.forEach(::destroySafely)
        return result
    }

    private fun hasAvailable(key: String): Boolean {
        val expired = mutableListOf<T>()
        val available = synchronized(lock) {
            val queue = pooled[key] ?: return@synchronized false
            val iterator = queue.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.impressed || entry.isExpired(now())) {
                    iterator.remove()
                    entries.remove(entry.ad)
                    expired += entry.ad
                }
            }
            if (queue.isEmpty()) pooled.remove(key)
            queue.isNotEmpty()
        }
        expired.forEach(::destroySafely)
        return available
    }

    private fun destroySafely(ad: T) {
        runCatching { destroy(ad) }
    }

    private class Entry<T : Any>(
        val key: String,
        val ad: T,
        val loadedAt: Long,
        var impressed: Boolean = false,
    ) {
        fun isExpired(currentTime: Long): Boolean = currentTime - loadedAt >= AD_TTL_MS
    }

    private companion object {
        const val MAX_POOLED_ADS_PER_KEY = 2
        const val AD_TTL_MS = 55L * 60L * 1000L
    }
}
