package com.aipose.camera.posematch.admob_ads

import android.Manifest

class Constant {
  companion object {
    const val TAG = "NextGenExample"
    // Debug builds only (see GoogleMobileAdsConsentManager). Get the value for YOUR device from
    // logcat: "Use new ConsentDebugSettings.Builder().addTestDeviceHashedId(...)". The old value
    // here was the sample app placeholder "ABCDEF012345", which is not a real device hash.
    const val TEST_DEVICE_HASHED_ID = "34DA7AE305CEE666514F096DAB5D4C77"
    var isAlreadyRateDialogShow = false
    const val notificationPermission = Manifest.permission.POST_NOTIFICATIONS
  }
}
