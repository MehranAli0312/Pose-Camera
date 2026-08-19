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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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

// SECTION 4. MAIN SCENIC CONTAINER (BOTTOM NAV BAR)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScenicContainer(
    viewModel: MainViewModel,
    onLaunchGalleryPicker: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    var activeTab by remember { mutableStateOf(NavTab.HOME) }
    // Camera is a pushed full-screen, opened by tapping a blueprint or the Home camera icon.
    var showCamera by remember { mutableStateOf(false) }
    // Exit confirmation bottom sheet (shown on back press from the Home tab).
    var showExitSheet by remember { mutableStateOf(false) }

    // --- Camera permission gating ---------------------------------------------------------
    fun isCamGranted() = ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.CAMERA
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    var cameraGranted by remember { mutableStateOf(isCamGranted()) }
    var askedOnce by remember { mutableStateOf(false) }

    val camPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraGranted = granted
        askedOnce = true
    }

    // Re-check when returning to the app (e.g. back from system Settings) so the card hides.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) cameraGranted = isCamGranted()
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    // Attention "bounce" applied to the permission card.
    val cardScale = remember { Animatable(1f) }
    fun bounceCard() {
        scope.launch {
            repeat(2) {
                cardScale.animateTo(1.07f, tween(150))
                cardScale.animateTo(0.96f, tween(150))
                cardScale.animateTo(1f, tween(130))
            }
        }
    }

    fun canPromptCamera(): Boolean =
        !askedOnce || (activity != null &&
            androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                activity, android.Manifest.permission.CAMERA
            ))

    // Card's Allow button: request while possible, otherwise deep-link to app settings.
    fun onAllowClick() {
        when {
            cameraGranted -> {}
            canPromptCamera() -> camPermLauncher.launch(android.Manifest.permission.CAMERA)
            else -> runCatching {
                context.startActivity(
                    Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                )
            }
        }
    }

    // Anything that opens the camera routes through here: open if allowed, otherwise ONLY
    // bounce the permission card to draw attention. The system dialog is requested solely
    // via the card's Allow button.
    fun tryOpenCamera() {
        if (cameraGranted) showCamera = true else bounceCard()
    }

    // Animate between the camera (pushed screen) and the tabbed container.
    AnimatedContent(
        targetState = showCamera,
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally(tween(300)) { it } + fadeIn(tween(300))) togetherWith
                        (fadeOut(tween(200)))
            } else {
                (fadeIn(tween(250))) togetherWith
                        (slideOutHorizontally(tween(300)) { it } + fadeOut(tween(250)))
            }
        },
        label = "camera_transition"
    ) { inCamera ->
        if (inCamera) {
            BackHandler { showCamera = false }
            PoseCameraScreen(
                viewModel = viewModel,
                onBack = { showCamera = false },
                onGoHome = {
                    activeTab = NavTab.HOME
                    showCamera = false
                }
            )
        } else {
            // Back: from a sub-tab return to Home; from Home ask to exit.
            BackHandler {
                if (activeTab != NavTab.HOME) activeTab = NavTab.HOME
                else showExitSheet = true
            }
            Scaffold(
                bottomBar = {
                  Column {
                    // Camera-permission prompt card — only while permission is not granted.
                    AnimatedVisibility(
                        visible = !cameraGranted,
                        enter = fadeIn(tween(200)) + expandVertically(tween(220)),
                        exit = fadeOut(tween(160)) + shrinkVertically(tween(200))
                    ) {
                        CameraPermissionCard(scale = cardScale.value, onAllow = { onAllowClick() })
                    }
                    NavigationBar(
                        containerColor = Color(0xFF0C0C0F),
                        tonalElevation = 8.dp,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        NavTab.entries.forEach { tab ->
                            val isSelected = activeTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { activeTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) AccentCopper else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(
                                            when (tab) {
                                                NavTab.HOME -> R.string.nav_home
                                                NavTab.HISTORY -> R.string.nav_gallery
                                                NavTab.SETTINGS -> R.string.nav_settings
                                            }
                                        ),
                                        color = if (isSelected) AccentCopper else Color.Gray,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = AccentCopper.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                  }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(StudioBackgroundGradient)
                ) {
                    // Smooth crossfade/slide between tab destinations.
                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = {
                            (fadeIn(tween(220)) + slideInVertically(tween(260)) { it / 14 }) togetherWith
                                    fadeOut(tween(160))
                        },
                        label = "tab_transition"
                    ) { tab ->
                        when (tab) {
                            NavTab.HOME -> HomeScreen(
                                viewModel = viewModel,
                                onOpenCamera = { tryOpenCamera() },
                                onLaunchGalleryPicker = onLaunchGalleryPicker
                            )

                            NavTab.HISTORY -> HistoryScreen(
                                viewModel,
                                onNavigateToCamera = { tryOpenCamera() })

                            NavTab.SETTINGS -> SettingsScreen(viewModel)
                        }
                    }
                }
            }
        }
    }

    // Modern exit confirmation — compact bottom sheet with Exit / Cancel.
    if (showExitSheet) {
        ModalBottomSheet(
            onDismissRequest = { showExitSheet = false },
            containerColor = Color(0xFF161619),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(AccentCopper.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = AccentCopper,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    stringResource(R.string.exit_title),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    stringResource(R.string.exit_message),
                    color = Color.Gray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(22.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showExitSheet = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, GlassWhite)
                    ) {
                        Text(
                            stringResource(R.string.action_cancel),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = {
                            showExitSheet = false
                            (context as? Activity)?.finish()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCopper)
                    ) {
                        Text(stringResource(R.string.action_exit), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Warning-style banner shown above the bottom bar while camera permission is missing.
@Composable
private fun CameraPermissionCard(scale: Float, onAllow: () -> Unit) {
    val warn = Color(0xFFED8B4E)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF2A1A12), Color(0xFF1B1B21))))
            .border(1.dp, warn.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(warn.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = warn, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.perm_camera_title),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                stringResource(R.string.perm_camera_desc),
                color = Color.Gray,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Button(
            onClick = onAllow,
            colors = ButtonDefaults.buttonColors(containerColor = warn),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.height(38.dp).testTag("camera_permission_allow")
        ) {
            Text(stringResource(R.string.perm_allow), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
