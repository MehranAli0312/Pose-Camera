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

// SECTION 8. HISTORY SCREEN — searchable, grouped by location, with a full-screen detail viewer.
@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onNavigateToCamera: () -> Unit
) {
    val allHistory by viewModel.capturedHistory.collectAsState()
    val history by viewModel.filteredHistory.collectAsState()
    val query by viewModel.historyQuery.collectAsState()

    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    fun dismissKeyboard() {
        keyboard?.hide()
        focusManager.clearFocus()
    }

    // Reset search whenever Gallery leaves composition (tab switch, camera) so returning
    // never shows stale text or a stuck results view.
    DisposableEffect(Unit) {
        onDispose { viewModel.setHistoryQuery("") }
    }

    var selectedPhoto by remember { mutableStateOf<CapturedPhoto?>(null) }
    var seeAllLocation by remember { mutableStateOf<String?>(null) }
    // Keep the open detail view in sync with DB updates (e.g. favorite toggled).
    val liveSelected = selectedPhoto?.let { sel -> allHistory.firstOrNull { it.id == sel.id } }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.history_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = stringResource(R.string.history_subtitle, allHistory.size),
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search bar — location, date/time, score, name.
            TextField(
                value = query,
                onValueChange = { viewModel.setHistoryQuery(it) },
                placeholder = {
                    Text(
                        stringResource(R.string.history_search_hint),
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setHistoryQuery(""); dismissKeyboard() }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("history_search_bar"),
                // Filled TextField: kill the bottom indicator ("blue line") in every state.
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SoftCardGray,
                    unfocusedContainerColor = SoftCardGray,
                    disabledContainerColor = SoftCardGray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = AccentCopper,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { dismissKeyboard() })
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (query.isEmpty()) Icons.Default.HistoryToggleOff else Icons.Default.SearchOff,
                            contentDescription = "Empty",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (query.isEmpty()) stringResource(R.string.history_empty)
                            else stringResource(R.string.history_no_match, query),
                            color = Color.Gray
                        )
                        if (query.isEmpty()) {
                            Text(
                                stringResource(R.string.history_empty_sub),
                                color = Color.DarkGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                val grouped = remember(history) { history.groupBy { it.locationName } }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
                ) {
                    grouped.forEach { (location, photos) ->
                        item(key = "header_$location") {
                            // Location header — pin + name + count on the left, "See all" on the right.
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = AccentCopper,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        location,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AccentCopper.copy(alpha = 0.18f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "${photos.size}",
                                            color = AccentCopper,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                if (photos.size > 6) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                dismissKeyboard(); seeAllLocation = location
                                            }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                            .testTag("see_all_$location")
                                    ) {
                                        Text(
                                            stringResource(R.string.see_all),
                                            color = AccentCopper,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = AccentCopper,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                        // Only the first 6 photos on the main screen.
                        item(key = "grid_$location") {
                            PhotoGridColumn(
                                photos = photos.take(6),
                                onClick = { dismissKeyboard(); selectedPhoto = it })
                        }
                    }
                }
            }
        }

        // "See all" — full album for one location, same grid flow.
        AnimatedVisibility(
            visible = seeAllLocation != null,
            enter = slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)),
            exit = slideOutHorizontally(tween(300)) { it } + fadeOut(tween(250))
        ) {
            val loc = seeAllLocation
            if (loc != null) {
                val albumPhotos =
                    remember(history, loc) { history.filter { it.locationName == loc } }
                LocationAlbumScreen(
                    location = loc,
                    photos = albumPhotos,
                    onBack = { seeAllLocation = null },
                    onPhotoClick = { selectedPhoto = it }
                )
            }
        }

        // Full-screen detail viewer.
        AnimatedVisibility(
            visible = liveSelected != null,
            enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.94f, animationSpec = tween(260)),
            exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.94f, animationSpec = tween(200))
        ) {
            liveSelected?.let { photo ->
                PhotoDetailScreen(
                    photo = photo,
                    onBack = { selectedPhoto = null },
                    onToggleFavorite = { viewModel.toggleFavoritePhoto(photo) },
                    onDelete = {
                        viewModel.deletePhoto(photo)
                        selectedPhoto = null
                    }
                )
            }
        }

        // Hardware back: close detail first, then album.
        BackHandler(enabled = selectedPhoto != null || seeAllLocation != null) {
            when {
                selectedPhoto != null -> selectedPhoto = null
                seeAllLocation != null -> seeAllLocation = null
            }
        }
    }
}
// Full album for a single location — same 3-column grid + tap-to-detail flow.
@Composable
internal fun LocationAlbumScreen(
    location: String,
    photos: List<CapturedPhoto>,
    onBack: () -> Unit,
    onPhotoClick: (CapturedPhoto) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBackgroundGradient)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("album_back")) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = AccentCopper,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    location,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${photos.size} ${stringResource(R.string.frames)}",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
        ) {
            photos.chunked(3).forEachIndexed { rowIdx, rowPhotos ->
                item(key = "album_row_$rowIdx") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPhotos.forEach { photo ->
                            HistoryThumbnail(
                                photo = photo,
                                modifier = Modifier.weight(1f),
                                onClick = { onPhotoClick(photo) })
                        }
                        repeat(3 - rowPhotos.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}
// Non-lazy chunked 3-column grid for a small photo set (used inside LazyColumn items).
@Composable
internal fun PhotoGridColumn(
    photos: List<CapturedPhoto>,
    onClick: (CapturedPhoto) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        photos.chunked(3).forEach { rowPhotos ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowPhotos.forEach { photo ->
                    HistoryThumbnail(
                        photo = photo,
                        modifier = Modifier.weight(1f),
                        onClick = { onClick(photo) })
                }
                repeat(3 - rowPhotos.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}
// Compact square thumbnail with score chip + favorite marker.
@Composable
internal fun HistoryThumbnail(
    photo: CapturedPhoto,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(SoftCardGray)
            .border(0.5.dp, GlassWhite, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("history_item_${photo.id}")
    ) {
        val imgFile = File(photo.imagePath)
        if (imgFile.exists()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(imgFile).crossfade(true)
                    .build(),
                contentDescription = photo.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.BrokenImage,
                    contentDescription = "Missing",
                    tint = Color.DarkGray
                )
            }
        }

        // Score chip
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                "${photo.matchScore}%",
                color = if (photo.matchScore >= 80) Color(0xFF81C784) else Color(0xFFFFB74D),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (photo.isFavorite) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = "Favorite",
                tint = AccentCopper,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(16.dp)
            )
        }

        // Date footer
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Text(
                SimpleDateFormat(
                    "MMM dd • hh:mm a",
                    Locale.getDefault()
                ).format(Date(photo.dateTimestamp)),
                color = Color.White,
                fontSize = 8.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}
// Full-screen photo detail with metadata + actions.
@Composable
fun PhotoDetailScreen(
    photo: CapturedPhoto,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
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
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                photo.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Balances the back button so the title stays centered (favorite removed).
            Spacer(modifier = Modifier.size(48.dp))
        }

        // Image — compact preview.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            val imgFile = File(photo.imagePath)
            if (imgFile.exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(imgFile).crossfade(true).build(),
                    contentDescription = photo.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    Icons.Default.BrokenImage,
                    contentDescription = "Missing",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        // Metadata card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SoftCardGray)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DetailRow(
                Icons.Default.LocationOn,
                stringResource(R.string.detail_location),
                photo.locationName
            )
            DetailRow(
                Icons.Default.Schedule,
                stringResource(R.string.detail_captured),
                formatHistoryDate(photo.dateTimestamp)
            )
            DetailRow(
                Icons.Default.Analytics,
                stringResource(R.string.detail_match_score),
                "${photo.matchScore}%",
                valueColor = if (photo.matchScore >= 80) Color(0xFF81C784) else Color(0xFFFFB74D)
            )
            DetailRow(
                Icons.Default.Category,
                stringResource(R.string.detail_category),
                photo.category
            )

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${photo.title} • ${photo.locationName} • Match ${photo.matchScore}% — via Pose Match Camera"
                            )
                        }
                        context.startActivity(Intent.createChooser(share, context.getString(R.string.share_frame)))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCopper),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.action_share),
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.action_delete),
                        color = Color(0xFFEF5350),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    // Delete confirmation — modern dialog with Delete / Cancel.
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF161619),
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFEF5350)) },
            title = { Text(stringResource(R.string.delete_title), color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_message), color = Color.Gray, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.action_delete), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }, shape = RoundedCornerShape(12.dp)) {
                    Text(stringResource(R.string.action_cancel), color = Color.White)
                }
            }
        )
    }
}
@Composable
internal fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.width(96.dp))
        Text(
            value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
