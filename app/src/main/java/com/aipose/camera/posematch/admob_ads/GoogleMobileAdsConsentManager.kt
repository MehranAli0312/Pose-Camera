package com.aipose.camera.posematch.admob_ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.aipose.camera.posematch.BuildConfig

class GoogleMobileAdsConsentManager private constructor(context: Context) {
  private val consentInformation: ConsentInformation =
    UserMessagingPlatform.getConsentInformation(context)

  fun interface OnConsentGatheringCompleteListener {
    fun canWeRequestAds(boolean: Boolean)
  }

  val canRequestAds: Boolean
    get() = consentInformation.canRequestAds()

 val isPrivacyOptionsRequired: Boolean
    get() =
      consentInformation.privacyOptionsRequirementStatus ==
              ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

  fun gatherConsent(
    activity: Activity,
    onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener,
  ) {
    Log.d(Constant.TAG, "gatherConsent: started")

    // ConsentDebugSettings is a TESTING-only facility. It was previously applied in EVERY
    // build with a placeholder hashed id copied from the sample app ("ABCDEF012345"), which
    // is not a real device hash — shipping debug wiring, and a malformed id, to production.
    // Restrict it to debug builds; release now sends plain consent request parameters.
    val paramsBuilder = ConsentRequestParameters.Builder()
    if (BuildConfig.DEBUG) {
      paramsBuilder.setConsentDebugSettings(
        ConsentDebugSettings.Builder(activity)
          .addTestDeviceHashedId(Constant.TEST_DEVICE_HASHED_ID)
          .build()
      )
    }
    val params = paramsBuilder.build()
    consentInformation.requestConsentInfoUpdate(
      activity,
      params,
      {
        Log.d(Constant.TAG, "requestConsentInfoUpdate: success")
       loadAndShowConsentFormIfRequired(activity, onConsentGatheringCompleteListener)
      },
      { requestConsentError ->
        Log.d(Constant.TAG, "requestConsentInfoUpdate: error ${requestConsentError.errorCode}: ${requestConsentError.message}")
       onConsentGatheringCompleteListener.canWeRequestAds(canRequestAds)
      },
    )
  }

  private fun loadAndShowConsentFormIfRequired(
    activity: Activity,
    onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener,
  ) {
    Log.d(Constant.TAG, "loadAndShowConsentFormIfRequired: checking")
    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
      if (formError != null) {
        Log.d(Constant.TAG, "loadAndShowConsentFormIfRequired: error ${formError.errorCode}: ${formError.message}")
      } else {
        Log.d(Constant.TAG, "loadAndShowConsentFormIfRequired: success")
      }
      onConsentGatheringCompleteListener.canWeRequestAds(canRequestAds)
    }
  }

  /** Helper method to call the UMP SDK method to show the privacy options form. */
  fun showPrivacyOptionsForm(
    activity: Activity,
    onConsentFormDismissedListener: ConsentForm.OnConsentFormDismissedListener,
  ) {
    UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener)
  }

  companion object {
    @Volatile private var instance: GoogleMobileAdsConsentManager? = null

    fun getInstance(context: Context) =
      instance
        ?: synchronized(this) {
          instance ?: GoogleMobileAdsConsentManager(context).also { instance = it }
        }
  }
}
