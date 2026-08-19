package com.aipose.camera.posematch.ui.screens

import android.annotation.SuppressLint
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import androidx.camera.core.ImageAnalysis
import com.aipose.camera.posematch.domain.PoseDetectorProcessor
import java.util.concurrent.Executors
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.graphics.PointF
import android.net.Uri
import android.provider.MediaStore
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aipose.camera.posematch.data.CapturedPhoto
import com.aipose.camera.posematch.data.CustomPose
import com.aipose.camera.posematch.data.PoseItem
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.atan2
import kotlin.math.roundToInt
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.RenderEffect
import android.media.ExifInterface
import android.view.Surface
import androidx.camera.core.ImageCaptureException
import com.aipose.camera.posematch.domain.PhotoFilters
import com.aipose.camera.posematch.domain.SubjectExtractor
import com.aipose.camera.posematch.data.LocationUtils
import com.aipose.camera.posematch.data.PlaceInfo
import com.aipose.camera.posematch.data.LocaleHelper
import com.aipose.camera.posematch.ui.theme.AppThemeState
import com.aipose.camera.posematch.ui.viewmodel.formatHistoryDate
import com.aipose.camera.posematch.R
import androidx.activity.compose.BackHandler
import androidx.compose.ui.res.stringResource
import coil.imageLoader
import coil.request.SuccessResult
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor

// Writes the raw framed JPEG via CameraX and hands the path back (no processing).
internal fun takePhotoToFile(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    flashMode: String,
    onSaved: (String) -> Unit
) {
    imageCapture.flashMode = when (flashMode) {
        "On" -> ImageCapture.FLASH_MODE_ON
        "Auto" -> ImageCapture.FLASH_MODE_AUTO
        else -> ImageCapture.FLASH_MODE_OFF
    }
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val file = File(context.getExternalFilesDir(null), "SNAP_$timeStamp.jpg")
    val options = ImageCapture.OutputFileOptions.Builder(file).build()

    imageCapture.takePicture(options, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
            ContextCompat.getMainExecutor(context).execute { onSaved(file.absolutePath) }
        }

        override fun onError(exc: ImageCaptureException) {
            exc.printStackTrace()
            ContextCompat.getMainExecutor(context).execute {
                Toast.makeText(context, context.getString(R.string.toast_capture_failed, exc.message ?: ""), Toast.LENGTH_SHORT).show()
            }
        }
    })
}
// Bakes the chosen filter into the file (if any), mirrors to the gallery, and records history.
// Runs on the ViewModel scope (not the screen's) so persistence survives navigating home
// immediately after Save.
internal fun savePhoto(
    context: Context,
    file: File,
    filterMatrix: FloatArray?,
    poseTitle: String,
    viewModel: MainViewModel,
    rotationDeg: Int = 0,
    cropAspect: Float? = null
) {
    if (filterMatrix != null || rotationDeg % 360 != 0 || cropAspect != null) {
        try {
            bakeFilterIntoFile(file, filterMatrix, rotationDeg, cropAspect)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    val appContext = context.applicationContext
    viewModel.viewModelScope.launch {
        val place = LocationUtils.resolvePlace(appContext)
        withContext(Dispatchers.IO) {
            LocationUtils.saveToGallery(appContext, file, place.name, file.name)
        }
        viewModel.capturePhoto(file.absolutePath, poseTitle, place)
        Toast.makeText(appContext, appContext.getString(R.string.toast_saved_location, place.name), Toast.LENGTH_SHORT).show()
    }
}
// Applies a color-matrix look to an already-saved JPEG, preserving upright orientation.
internal fun bakeFilterIntoFile(
    file: File,
    matrix: FloatArray?,
    rotationDeg: Int = 0,
    cropAspect: Float? = null
) {
    val orientation = try {
        ExifInterface(file.absolutePath).getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )
    } catch (e: Exception) {
        ExifInterface.ORIENTATION_NORMAL
    }

    val decoded = BitmapFactory.decodeFile(file.absolutePath) ?: return
    var bmp = applyExifRotation(decoded, orientation)
    if (rotationDeg % 360 != 0) bmp = rotateBitmap(bmp, rotationDeg)
    if (cropAspect != null) bmp = centerCropToAspect(bmp, cropAspect)
    if (matrix != null) bmp = PhotoFilters.apply(bmp, matrix)
    FileOutputStream(file).use { out ->
        bmp.compress(Bitmap.CompressFormat.JPEG, 95, out)
    }
}

// Rotate the bitmap by [deg] (90° steps).
private fun rotateBitmap(src: Bitmap, deg: Int): Bitmap {
    val m = Matrix().apply { postRotate(deg.toFloat()) }
    return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
}

// Center-crop the bitmap to the given aspect ratio (width / height).
private fun centerCropToAspect(src: Bitmap, aspect: Float): Bitmap {
    val w = src.width
    val h = src.height
    val current = w.toFloat() / h
    return if (current > aspect) {
        val newW = (h * aspect).toInt().coerceIn(1, w)
        Bitmap.createBitmap(src, (w - newW) / 2, 0, newW, h)
    } else {
        val newH = (w / aspect).toInt().coerceIn(1, h)
        Bitmap.createBitmap(src, 0, (h - newH) / 2, w, newH)
    }
}
internal fun applyExifRotation(src: Bitmap, orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        else -> return src
    }
    return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
}
// Loads the reference overlay (drawable name, local file path, or URL) as a software bitmap
// for runtime look extraction. Uses the shared Coil loader so it hits cache when possible.
internal suspend fun loadOverlayBitmap(context: Context, image: String, size: Int = 256): Bitmap? {
    // Bundled assets: decode straight from AssetManager (reliable offline, unlike Coil's asset uri).
    assetPathOf(image)?.let { assetPath ->
        return withContext(Dispatchers.IO) { decodeAsset(context, assetPath, sample = 1) }
    }
    return try {
        val model: Any = when {
            image.startsWith("http") -> image
            image.startsWith("file://") -> image
            image.contains("/") -> File(image)
            else -> {
                val id = context.resources.getIdentifier(image, "drawable", context.packageName)
                if (id == 0) return null else id
            }
        }
        val request = ImageRequest.Builder(context)
            .data(model)
            .allowHardware(false)
            .size(size)
            .build()
        val result = context.imageLoader.execute(request)
        (result as? SuccessResult)?.drawable?.toBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
