package com.aipose.camera.posematch.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.aipose.camera.posematch.R
import com.example.common.showToast

fun Context.getAppLink() = "https://play.google.com/store/apps/details?id=${this.packageName}"
fun Context.shareApp() = "${resources.getString(R.string.app_name)}: ${getAppLink()}"

const val PRIVACY_POLICY = "https://sites.google.com/view/posematchcamera"
const val TERMS_OF_SERVICE = PRIVACY_POLICY

fun Context.contact() {
    try {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(
                Intent.EXTRA_EMAIL, arrayOf("slife0667@gmail.com")
            )
            putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name))
        }
        startActivity(emailIntent)
    } catch (e: ActivityNotFoundException) {
        showToast(getString(R.string.error_not_found))
    }
}
