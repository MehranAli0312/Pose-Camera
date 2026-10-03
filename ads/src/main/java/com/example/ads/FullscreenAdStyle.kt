package com.example.ads

sealed interface FullscreenAdStyle {

    val format: AdFormat?

    val isVisible: Boolean get() = format != null

    data object Hidden : FullscreenAdStyle {
        override val format: AdFormat? get() = null
    }

    data object Interstitial : FullscreenAdStyle {
        override val format: AdFormat get() = AdFormat.INTERSTITIAL
    }

    data object AppOpen : FullscreenAdStyle {
        override val format: AdFormat get() = AdFormat.APP_OPEN
    }

    data object Rewarded : FullscreenAdStyle {
        override val format: AdFormat get() = AdFormat.REWARDED
    }

    data object RewardedInterstitial : FullscreenAdStyle {
        override val format: AdFormat get() = AdFormat.REWARDED_INTERSTITIAL
    }
}
