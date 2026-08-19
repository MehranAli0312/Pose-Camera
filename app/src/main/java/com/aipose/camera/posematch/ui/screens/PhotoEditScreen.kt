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

// Result of an edit session: colour matrix + geometry (rotation in degrees, crop aspect w/h).
data class EditResult(val matrix: FloatArray?, val rotationDeg: Int, val cropAspect: Float?)

// The manual adjustment tools available in the editor's "Adjust" tab. Every tool maps to a
// linear color transform so the live preview and the saved JPEG match exactly.
internal enum class AdjustTool(val label: String, val icon: ImageVector) {
    Exposure("Exposure", Icons.Default.Exposure),
    Brightness("Brightness", Icons.Default.LightMode),
    Contrast("Contrast", Icons.Default.Contrast),
    Saturation("Saturation", Icons.Default.WaterDrop),
    Warmth("Warmth", Icons.Default.Thermostat),
    Tint("Tint", Icons.Default.Colorize),
    Hue("Hue", Icons.Default.Palette),
    Fade("Fade", Icons.Default.Gradient),
    // Geometry tools — no colour slider; they show their own controls below the rail.
    Rotate("Rotate", Icons.Default.RotateRight),
    Crop("Crop", Icons.Default.Crop)
}
// Post-capture editor — real-image filter previews + iPhone-style adjustments, then "Done".
@Composable
fun PhotoEditScreen(
    photoPath: String,
    autoFilterMatrix: FloatArray?,
    autoFilterColor: Int?,
    onDiscard: () -> Unit,
    onDone: (EditResult) -> Unit
) {
    val context = LocalContext.current
    var selectedFilterId by remember { mutableStateOf(PhotoFilters.ID_AUTO) }
    var editMode by remember { mutableStateOf(0) } // 0 = Filters, 1 = Adjust
    var activeTool by remember { mutableStateOf(AdjustTool.Exposure) }
    // Geometry edits (Adjust tab): rotation in 90° steps, and an optional crop aspect (w/h).
    var rotationDeg by remember { mutableStateOf(0) }
    var cropAspect by remember { mutableStateOf<Float?>(null) }

    // One slider value per tool (neutral 0f). A map scales cleanly as tools are added.
    val adjustments = remember { mutableStateMapOf<AdjustTool, Float>() }
    fun adj(tool: AdjustTool): Float = adjustments[tool] ?: 0f

    val filterMatrix = remember(selectedFilterId, autoFilterMatrix) {
        PhotoFilters.matrixFor(selectedFilterId, autoFilterMatrix)
    }
    val adjustMatrix = PhotoFilters.adjustmentMatrix(
        exposure = adj(AdjustTool.Exposure),
        brightness = adj(AdjustTool.Brightness),
        contrast = adj(AdjustTool.Contrast),
        saturation = adj(AdjustTool.Saturation),
        warmth = adj(AdjustTool.Warmth),
        tint = adj(AdjustTool.Tint),
        hue = adj(AdjustTool.Hue),
        fade = adj(AdjustTool.Fade)
    )
    val finalMatrix = PhotoFilters.compose(filterMatrix, adjustMatrix)
    val previewFilter = finalMatrix?.let {
        ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix(it.copyOf()))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060608))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onDiscard, modifier = Modifier.testTag("review_discard")) {
                Icon(Icons.Default.Close, contentDescription = "Discard", tint = Color.White)
            }
            Text(stringResource(R.string.title_edit), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            // Save — bakes filter + rotation + crop, then opens the success screen.
            Button(
                onClick = { onDone(EditResult(finalMatrix, rotationDeg, cropAspect)) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCopper),
                shape = RoundedCornerShape(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("review_done")
            ) {
                Text(
                    stringResource(R.string.action_save),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Live-filtered image preview (ColorFilter works on all API levels; full quality on save)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(File(photoPath)).crossfade(true).build(),
                contentDescription = "Captured frame",
                modifier = Modifier
                    .then(if (cropAspect != null) Modifier.aspectRatio(cropAspect!!) else Modifier.fillMaxSize())
                    .clip(RoundedCornerShape(10.dp))
                    .graphicsLayer { rotationZ = rotationDeg.toFloat() },
                contentScale = if (cropAspect != null) ContentScale.Crop else ContentScale.Fit,
                colorFilter = previewFilter
            )
        }

        // Filters / Adjust toggle on the left, compact revert on the right end.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SoftCardGray)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf(
                    "Filters" to stringResource(R.string.tab_filters),
                    "Adjust" to stringResource(R.string.tab_adjust)
                ).forEachIndexed { index, (tag, title) ->
                    val selected = editMode == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) AccentCopper else Color.Transparent)
                            .clickable { editMode = index }
                            .padding(horizontal = 20.dp, vertical = 7.dp)
                            .testTag("edit_tab_$tag"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            title,
                            color = if (selected) Color.White else Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            // Revert — resets filter, adjustments, rotation and crop.
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SoftCardGray)
                    .clickable {
                        selectedFilterId = PhotoFilters.ID_ORIGINAL
                        adjustments.clear()
                        rotationDeg = 0
                        cropAspect = null
                    }
                    .testTag("review_reset"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.action_reset),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // The active editing panel
        Box(modifier = Modifier
            .fillMaxWidth()) {
            if (editMode == 0) {
                // Real-image filter previews: each chip shows THIS photo graded by that look.
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(PhotoFilters.strip) { filter ->
                        val isSelected = selectedFilterId == filter.id
                        val chipMatrix = PhotoFilters.matrixFor(filter.id, autoFilterMatrix)
                        val chipFilter = chipMatrix?.let {
                            ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix(it.copyOf()))
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(64.dp)
                                .clickable { selectedFilterId = filter.id }
                                .testTag("review_filter_${filter.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) AccentCopper else GlassWhite,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(File(photoPath))
                                        .size(120).build(),
                                    contentDescription = filter.label,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop,
                                    colorFilter = chipFilter
                                )
                                if (filter.id == PhotoFilters.ID_AUTO) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                filter.label,
                                color = if (isSelected) AccentCopper else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                // iPhone-style adjustments: a scrollable tool rail + one slider for the active tool.
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(AdjustTool.entries) { tool ->
                            val active = activeTool == tool
                            val touched = when (tool) {
                                AdjustTool.Rotate -> rotationDeg != 0
                                AdjustTool.Crop -> cropAspect != null
                                else -> adj(tool) != 0f
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(58.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (active) AccentCopper.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable { activeTool = tool }
                                    .padding(vertical = 6.dp)
                                    .testTag("adjust_${tool.name}")
                            ) {
                                Box(contentAlignment = Alignment.TopEnd) {
                                    Icon(
                                        tool.icon,
                                        contentDescription = tool.label,
                                        tint = if (active) AccentCopper else if (touched) Color.White else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    if (touched) {
                                        Box(
                                            modifier = Modifier
                                                .offset(x = 3.dp, y = (-2).dp)
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(AccentCopper)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    tool.label,
                                    color = if (active) AccentCopper else Color.Gray,
                                    fontSize = 9.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    // Control adapts to the active tool: slider for colour, buttons/chips for geometry.
                    when (activeTool) {
                        AdjustTool.Rotate -> Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            listOf(
                                Icons.Default.RotateLeft to ("90° Left" to 270),
                                Icons.Default.RotateRight to ("90° Right" to 90)
                            ).forEach { (icon, data) ->
                                val (label, delta) = data
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SoftCardGray)
                                        .clickable { rotationDeg = (rotationDeg + delta) % 360 }
                                        .padding(vertical = 11.dp)
                                        .testTag("rotate_$delta"),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(label, color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                        AdjustTool.Crop -> Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("Free" to null, "1:1" to 1f, "4:5" to 0.8f, "16:9" to (16f / 9f), "9:16" to (9f / 16f))
                                .forEach { (label, ratio) ->
                                    val sel = cropAspect == ratio
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (sel) AccentCopper else SoftCardGray)
                                            .clickable { cropAspect = ratio }
                                            .padding(horizontal = 14.dp, vertical = 9.dp)
                                            .testTag("crop_$label"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(label, color = if (sel) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                        }
                        else -> {
                            val value = adj(activeTool)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Slider(
                                    value = value,
                                    onValueChange = { v -> adjustments[activeTool] = v },
                                    valueRange = -1f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = AccentCopper,
                                        activeTrackColor = AccentCopper,
                                        inactiveTrackColor = SoftCardGray
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("adjust_slider")
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "${(value * 100).toInt()}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}
// Post-save success screen — shows the saved photo, a success banner, and compact Home + Share.
@Composable
fun PhotoSuccessScreen(
    photoPath: String,
    onHome: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // The saved image (the chosen filter is already baked into the file).
        AsyncImage(
            model = ImageRequest.Builder(context).data(File(photoPath)).crossfade(true).build(),
            contentDescription = "Saved photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // Success banner on top.
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF2E7D32).copy(alpha = 0.92f))
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.success_saved), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        // Home + Share — identical compact glass buttons.
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 26.dp),
            horizontalArrangement = Arrangement.spacedBy(36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(1.dp, GlassWhite, CircleShape)
                        .clickable { onHome() }
                        .testTag("success_home"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(stringResource(R.string.nav_home), color = Color.White, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(1.dp, GlassWhite, CircleShape)
                        .clickable { onShare() }
                        .testTag("success_share"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(stringResource(R.string.action_share), color = Color.White, fontSize = 11.sp)
            }
        }
    }
}
