package com.example.common

import android.content.Context
import android.widget.Toast

fun Context.showToast(mess: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, mess, duration).show()
}
