package com.aipose.camera.posematch.data.local

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.aipose.camera.posematch.domain.models.CaptureLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class DeviceLocationDataSource(private val context: Context) {

    suspend fun resolvePlace(): CaptureLocation = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) return@withContext EMPTY_LOCATION
        runCatching {
            val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val lastKnown = PROVIDERS.mapNotNull { provider ->
                runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
            }.maxByOrNull { it.time } ?: return@runCatching EMPTY_LOCATION
            CaptureLocation(
                name = reverseGeocode(lastKnown.latitude, lastKnown.longitude),
                latitude = lastKnown.latitude,
                longitude = lastKnown.longitude
            )
        }.getOrDefault(EMPTY_LOCATION)
    }

    private fun hasLocationPermission(): Boolean = LOCATION_PERMISSIONS.any { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun reverseGeocode(latitude: Double, longitude: Double): String? = runCatching {
        @Suppress("DEPRECATION")
        val address = Geocoder(context, Locale.getDefault())
            .getFromLocation(latitude, longitude, 1)
            ?.firstOrNull()
        address?.locality
            ?: address?.subAdminArea
            ?: address?.adminArea
            ?: address?.subLocality
            ?: address?.featureName
    }.getOrNull()

    private companion object {
        val EMPTY_LOCATION = CaptureLocation(null, null, null)
        val LOCATION_PERMISSIONS = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val PROVIDERS = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
    }
}
