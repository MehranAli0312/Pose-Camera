package com.example.common

import android.content.Context
import android.util.Log
import android.widget.Toast

fun Context.showToast(mess: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, mess, duration).show()
}

fun showLog(mess: String) {
    Log.e("Mehran_TAG", "OR : $mess")
}
