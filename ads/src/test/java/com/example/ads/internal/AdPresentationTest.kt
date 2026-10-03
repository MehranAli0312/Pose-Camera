package com.example.ads.internal

import com.example.ads.AdResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdPresentationTest {

    @Test
    fun `dismiss without a reward reports Shown`() = runBlocking {
        val presentation = AdPresentation()

        presentation.onShown()
        presentation.onDismissed()

        assertEquals(AdResult.Shown, presentation.await())
    }

    @Test
    fun `reward confirmed by the SDK before dismiss is reported`() = runBlocking {
        val presentation = AdPresentation()

        presentation.onShown()
        presentation.onReward("coins", 10)
        presentation.onDismissed()

        val result = presentation.await()
        assertEquals(AdResult.Rewarded("coins", 10), result)
        assertTrue(result.wasRewarded)
    }

    @Test
    fun `failure never grants a reward even if one was reported first`() = runBlocking {
        val presentation = AdPresentation()

        presentation.onShown()
        presentation.onReward("coins", 10)
        presentation.onFailed("presentation error")

        val result = presentation.await()
        assertEquals(AdResult.Failed("presentation error"), result)
        assertFalse(result.wasRewarded)
    }

    @Test
    fun `a fresh presentation never sees a reward from a previous one`() = runBlocking {
        val rewarded = AdPresentation()
        rewarded.onReward("coins", 10)
        rewarded.onDismissed()
        assertTrue(rewarded.await().wasRewarded)

        val next = AdPresentation()
        next.onDismissed()

        assertEquals(AdResult.Shown, next.await())
    }

    @Test
    fun `result is emitted exactly once - a later completion attempt is ignored`() = runBlocking {
        val presentation = AdPresentation()

        presentation.onDismissed()
        presentation.onReward("coins", 10)
        presentation.onFailed("should not overwrite the dismiss result")

        assertEquals(AdResult.Shown, presentation.await())
    }

    @Test
    fun `onShown invokes the supplied callback exactly once`() {
        var invocations = 0
        val presentation = AdPresentation(onShownCallback = { invocations++ })

        presentation.onShown()

        assertEquals(1, invocations)
    }
}
