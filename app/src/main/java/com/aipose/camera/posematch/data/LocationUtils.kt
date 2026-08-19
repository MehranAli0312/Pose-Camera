package com.aipose.camera.posematch.data

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/** Resolved place for a capture. */
data class PlaceInfo(
    val name: String,
    val latitude: Double?,
    val longitude: Double?
)

/**
 * Free, on-device/no-API-key location + reverse geocoding, and gallery persistence via MediaStore.
 * Photos land in the device gallery under Pictures/PoseMatch/<Location> so each location reads as
 * its own album/folder.
 */
object LocationUtils {

    private const val ALBUM_ROOT = "PoseMatch"

    private fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /** Best-effort current place. Falls back to "Unknown Location" when unavailable. */
    suspend fun resolvePlace(context: Context): PlaceInfo = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) return@withContext PlaceInfo("Unknown Location", null, null)
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            val best = providers.mapNotNull { p ->
                try {
                    @Suppress("MissingPermission")
                    lm.getLastKnownLocation(p)
                } catch (e: SecurityException) {
                    null
                }
            }.maxByOrNull { it.time }
                ?: return@withContext PlaceInfo("Unknown Location", null, null)

            val name = reverseGeocode(context, best.latitude, best.longitude)
            PlaceInfo(name, best.latitude, best.longitude)
        } catch (e: Exception) {
            e.printStackTrace()
            PlaceInfo("Unknown Location", null, null)
        }
    }

    /** Converts coordinates into a readable place name (locality / district / region). */
    private fun reverseGeocode(context: Context, lat: Double, lng: Double): String {
        return try {
            @Suppress("DEPRECATION")
            val addresses = Geocoder(context, Locale.getDefault()).getFromLocation(lat, lng, 1)
            val a = addresses?.firstOrNull() ?: return "Unknown Location"
            a.locality
                ?: a.subAdminArea
                ?: a.adminArea
                ?: a.subLocality
                ?: a.featureName
                ?: "Unknown Location"
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown Location"
        }
    }

    private fun sanitize(folder: String): String =
        folder.trim().replace(Regex("[^A-Za-z0-9 _-]"), "").ifBlank { "Unknown Location" }

    /**
     * Copies [source] into the public gallery under Pictures/PoseMatch/<location>.
     * Returns the gallery Uri (or null on failure). Original app-sandbox file is left untouched.
     */
    fun saveToGallery(context: Context, source: File, locationName: String, displayName: String): Uri? {
        val folder = sanitize(locationName)
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/$ALBUM_ROOT/$folder"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return null
                resolver.openOutputStream(uri)?.use { out ->
                    source.inputStream().use { it.copyTo(out) }
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                uri
            } else {
                @Suppress("DEPRECATION")
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val dir = File(picturesDir, "$ALBUM_ROOT/$folder").apply { mkdirs() }
                val dest = File(dir, displayName)
                source.inputStream().use { input -> dest.outputStream().use { input.copyTo(it) } }
                MediaScannerConnection.scanFile(context, arrayOf(dest.absolutePath), arrayOf("image/jpeg"), null)
                Uri.fromFile(dest)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
