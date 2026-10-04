package com.aipose.camera.posematch.ui.util

import android.content.Context
import android.view.View
import java.util.Locale

object LocaleHelper {
    fun onAttach(context: Context): Context = context
    fun setLocale(context: Context, language: String): Context = context
}

fun Context.isNetworkAvailable(): Boolean = true

val Context.sharedPrefrencesClass: SharedPrefsMock
    get() = SharedPrefsMock()

class SharedPrefsMock {
    val selectedLanguage: String? = "en"
}

fun View.beInvisible() {
    this.visibility = View.INVISIBLE
}
