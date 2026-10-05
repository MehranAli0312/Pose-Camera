package com.aipose.camera.posematch.ui.screens.onboard.models

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface OnboardPage {

    val key: String

    data class Step(val step: OnboardStep, val stepIndex: Int) : OnboardPage {
        override val key: String get() = step.stage.name
    }

    data object NativeAd : OnboardPage {
        override val key: String get() = NATIVE_AD_PAGE_KEY
    }
}

private const val NATIVE_AD_PAGE_KEY = "onboard_native_ad"

internal fun onboardPages(steps: List<OnboardStep>, adPageIndex: Int?): List<OnboardPage> {
    val stepPages = steps.mapIndexed { index, step -> OnboardPage.Step(step, index) }
    if (adPageIndex == null || adPageIndex !in 1..stepPages.lastIndex) return stepPages
    return stepPages.take(adPageIndex) + OnboardPage.NativeAd + stepPages.drop(adPageIndex)
}

internal fun List<OnboardPage>.stepIndexAt(page: Int): Int =
    take(page + 1).filterIsInstance<OnboardPage.Step>().lastOrNull()?.stepIndex ?: 0
