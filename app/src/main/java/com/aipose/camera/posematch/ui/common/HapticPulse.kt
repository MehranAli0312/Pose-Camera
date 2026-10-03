package com.aipose.camera.posematch.ui.common

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberHapticPulse(): (Long) -> Unit {
    val context = LocalContext.current
    val vibrator = remember(context) { context.vibrator() }
    return remember(vibrator) {
        { durationMillis ->
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(
                            durationMillis,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMillis)
                }
            }
            Unit
        }
    }
}

private fun Context.vibrator(): Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
} else {
    @Suppress("DEPRECATION")
    getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
}
