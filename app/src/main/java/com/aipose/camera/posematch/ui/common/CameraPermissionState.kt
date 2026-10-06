package com.aipose.camera.posematch.ui.common

import android.Manifest
import android.app.Activity
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

private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
private val LOCATION_PERMISSIONS = listOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

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

    var isGranted by remember { mutableStateOf(context.isGranted(CAMERA_PERMISSION)) }
    var isBlocked by remember { mutableStateOf(false) }
    var hasAskedSystem by rememberSaveable { mutableStateOf(false) }

    fun refresh() {
        isGranted = context.isGranted(CAMERA_PERMISSION)
        isBlocked = hasAskedSystem && !isGranted && activity?.isRationaleHidden(CAMERA_PERMISSION) == true
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { refresh() }

    fun launch(permissions: List<String>) {
        if (permissions.isEmpty()) return
        hasAskedSystem = true
        launcher.launch(permissions.toTypedArray())
    }

    LaunchedEffect(Unit) {
        if (hasAskedSystem) refresh() else launch(context.missingEntryPermissions(activity))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh()
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
                else -> launch(listOf(CAMERA_PERMISSION))
            }
        },
    )
}

private fun Context.missingEntryPermissions(activity: Activity?): List<String> = buildList {
    if (!isGranted(CAMERA_PERMISSION)) add(CAMERA_PERMISSION)
    if (shouldPromptLocation(activity)) addAll(LOCATION_PERMISSIONS)
}

private fun Context.shouldPromptLocation(activity: Activity?): Boolean =
    LOCATION_PERMISSIONS.none(::isGranted) &&
        activity?.isRationaleHidden(Manifest.permission.ACCESS_FINE_LOCATION) != false

private fun Activity.isRationaleHidden(permission: String): Boolean =
    !ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

private fun Context.isGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

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
