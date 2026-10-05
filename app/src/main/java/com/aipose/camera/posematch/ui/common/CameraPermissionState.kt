package com.aipose.camera.posematch.ui.common

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

private var hasAskedLocation = false

@Immutable
data class CameraPermissionState(
    val isGranted: Boolean,
    val isBlocked: Boolean,
    val request: () -> Unit,
)

@Composable
fun rememberCameraPermissionState(): CameraPermissionState {
    val context = LocalContext.current
    val activity = getActivity()
    val lifecycleOwner = LocalLifecycleOwner.current

    var isGranted by remember { mutableStateOf(context.isCameraGranted()) }
    var isBlocked by remember { mutableStateOf(false) }
    var hasAskedSystem by rememberSaveable { mutableStateOf(false) }

    fun refreshBlocked() {
        isBlocked = hasAskedSystem && !isGranted &&
            activity?.let {
                !ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
            } == true
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isGranted = granted
        hasAskedSystem = true
        refreshBlocked()
    }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        if (!isGranted && !hasAskedSystem) {
            hasAskedSystem = true
            cameraLauncher.launch(Manifest.permission.CAMERA)
        } else {
            refreshBlocked()
        }
        if (!hasAskedLocation && !context.isLocationGranted()) {
            hasAskedLocation = true
            locationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isGranted = context.isCameraGranted()
                refreshBlocked()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return CameraPermissionState(
        isGranted = isGranted,
        isBlocked = isBlocked,
        request = {
            when {
                isGranted -> Unit
                isBlocked -> context.openAppSettings()
                else -> {
                    hasAskedSystem = true
                    cameraLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        },
    )
}

private fun Context.isCameraGranted(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

private fun Context.isLocationGranted(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

private fun Context.openAppSettings() {
    runCatching {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}
