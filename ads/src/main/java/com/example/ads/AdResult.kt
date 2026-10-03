package com.example.ads

sealed interface AdResult {

    data object Shown : AdResult

    data class Rewarded(val type: String, val amount: Int) : AdResult

    data object NotEligible : AdResult

    data object NotAvailable : AdResult

    data class Failed(val message: String) : AdResult

    val wasShown: Boolean get() = this is Shown || this is Rewarded

    val wasRewarded: Boolean get() = this is Rewarded
}

sealed interface ConsentResult {

    data object CanRequestAds : ConsentResult

    data object CannotRequestAds : ConsentResult

    data class Error(val message: String) : ConsentResult
}
