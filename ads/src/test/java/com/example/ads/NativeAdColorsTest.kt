package com.example.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NativeAdColorsTest {

    @Test
    fun `rgb hex gets opaque alpha`() {
        assertEquals(0xFF1E88E5.toInt(), NativeAdColors.parseHex("#1E88E5"))
        assertEquals(0xFF1E88E5.toInt(), NativeAdColors.parseHex("1e88e5"))
    }

    @Test
    fun `argb hex keeps its alpha`() {
        assertEquals(0x801E88E5.toInt(), NativeAdColors.parseHex(" #801E88E5 "))
    }

    @Test
    fun `blank or malformed hex falls back to theme`() {
        assertNull(NativeAdColors.parseHex(""))
        assertNull(NativeAdColors.parseHex("#12345"))
        assertNull(NativeAdColors.parseHex("#GGGGGG"))
        assertNull(NativeAdColors.parseHex("blue"))
    }
}
