package com.example.ads.internal

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AdPoolTest {

    @Test
    fun unseenAdIsReusedWithoutAnotherLoad() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val destroyed = mutableListOf<TestAd>()
        val pool = AdPool(scope, destroyed::add) { 0L }
        var loads = 0
        val first = pool.acquire("unit") { TestAd(++loads) }

        pool.recycle(requireNotNull(first))
        val second = pool.acquire("unit") { TestAd(++loads) }

        assertSame(first, second)
        assertEquals(1, loads)
        assertTrue(destroyed.isEmpty())
        scope.cancel()
    }

    @Test
    fun impressedAdIsDestroyedInsteadOfRecycled() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val destroyed = mutableListOf<TestAd>()
        val pool = AdPool(scope, destroyed::add) { 0L }
        var loads = 0
        val first = requireNotNull(pool.acquire("unit") { TestAd(++loads) })

        pool.markImpressed(first)
        pool.recycle(first)
        val second = pool.acquire("unit") { TestAd(++loads) }

        assertEquals(listOf(first), destroyed)
        assertEquals(2, loads)
        assertTrue(first !== second)
        scope.cancel()
    }

    @Test
    fun concurrentAcquiresJoinOneLoad() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val pool = AdPool<TestAd>(scope, {}) { 0L }
        val loadStarted = CompletableDeferred<Unit>()
        val releaseLoad = CompletableDeferred<Unit>()
        var loads = 0
        suspend fun load(): TestAd {
            loads++
            loadStarted.complete(Unit)
            releaseLoad.await()
            return TestAd(loads)
        }

        val first = async { pool.acquire("unit", ::load) }
        loadStarted.await()
        val second = async { pool.acquire("unit", ::load) }
        yield()
        releaseLoad.complete(Unit)
        val results = listOf(first.await(), second.await())

        assertEquals(1, loads)
        assertEquals(1, results.count { it != null })
        scope.cancel()
    }

    private data class TestAd(val id: Int)
}
