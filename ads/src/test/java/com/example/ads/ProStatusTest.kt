package com.example.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProStatusTest {

    @Test
    fun `only a resolved free user is eligible for ads`() {
        assertTrue(ProStatus.FREE.isEligibleForAds)

        assertFalse("a paying user must never be eligible", ProStatus.PRO.isEligibleForAds)
        assertFalse(
            "an unresolved status must not be treated as free",
            ProStatus.UNKNOWN.isEligibleForAds,
        )
    }

    @Test
    fun `unknown is the only unresolved state`() {
        assertFalse(ProStatus.UNKNOWN.isResolved)
        assertTrue(ProStatus.PRO.isResolved)
        assertTrue(ProStatus.FREE.isResolved)
    }

    @Test
    fun `every status is either eligible or explicitly blocked, never undefined`() {
        ProStatus.entries.forEach { status ->
            val eligible = status.isEligibleForAds
            assertTrue(
                "$status must be eligible only when it is FREE",
                eligible == (status == ProStatus.FREE),
            )
        }
    }
}
