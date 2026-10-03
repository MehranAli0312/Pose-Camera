package com.example.ads.internal

import android.app.Activity
import android.content.Context
import com.example.ads.ConsentResult
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

internal class ConsentManager(
    private val context: Context,
    private val log: AdsLog,
) {

    private val consentInformation: ConsentInformation by lazy {
        UserMessagingPlatform.getConsentInformation(context)
    }

    fun canRequestAds(): Boolean = runCatching { consentInformation.canRequestAds() }
        .onFailure { log.w("Cached consent check failed", it) }
        .getOrDefault(false)

    suspend fun requestConsent(activity: Activity): ConsentResult =
        withContext(Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                val params = ConsentRequestParameters.Builder()
                    .setTagForUnderAgeOfConsent(false)
                    .build()

                val information = consentInformation

                var resumed = false
                fun finish(result: ConsentResult) {
                    if (!resumed && continuation.isActive) {
                        resumed = true
                        continuation.resume(result)
                    }
                }

                information.requestConsentInfoUpdate(
                    activity,
                    params,
                    {
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                            if (formError != null) {
                                log.w("Consent form error ${formError.errorCode}: ${formError.message}")
                            }
                            finish(
                                if (information.canRequestAds()) ConsentResult.CanRequestAds
                                else ConsentResult.CannotRequestAds
                            )
                        }
                    },
                    { requestError ->
                        log.w("Consent update failed ${requestError.errorCode}: ${requestError.message}")
                        finish(
                            if (information.canRequestAds()) ConsentResult.CanRequestAds
                            else ConsentResult.Error(requestError.message ?: "consent update failed")
                        )
                    },
                )
            }
        }
}
