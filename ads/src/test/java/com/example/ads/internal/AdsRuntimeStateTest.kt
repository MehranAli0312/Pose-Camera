package com.example.ads.internal

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdsRuntimeStateTest {

    @Test
    fun `second acquire fails while the first still holds the lock`() {
        val state = AdsRuntimeState()

        assertTrue(state.tryAcquireFullScreen())
        assertFalse(state.tryAcquireFullScreen())
    }
}
