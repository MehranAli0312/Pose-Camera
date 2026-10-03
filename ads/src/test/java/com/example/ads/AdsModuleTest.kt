package com.example.ads

import android.app.Application
import android.content.Context
import com.example.ads.di.adsModule
import com.example.ads.internal.BuildConfigAdUnitProvider
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

class AdsModuleTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `ads module graph resolves given the app-supplied bindings`() {
        adsModule.verify(
            extraTypes = listOf(
                Context::class,
                Application::class,
                AdsConfig::class,
                ProStatusProvider::class,
                AdSlotStyleProvider::class,
                AdUnitProvider::class,
            ),
        )
    }

    @Test
    fun `the debug build configuration supplies only Google test units`() {
        val provider = BuildConfigAdUnitProvider()

        AdFormat.entries.forEach { format ->
            val unit = provider.adUnitId(format, AdPlacement.Default)

            assertNotNull("$format has no ad unit in the debug build configuration", unit)
            assertTrue(
                "$format resolved to $unit, which is not a Google test unit",
                unit!!.startsWith(GOOGLE_TEST_PUBLISHER_ID),
            )
        }
    }

    private companion object {
        const val GOOGLE_TEST_PUBLISHER_ID = "ca-app-pub-3940256099942544/"
    }
}
