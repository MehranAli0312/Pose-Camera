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
import androidx.compose.ui.layout.onSizeChanged
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

// SECTION 6. POSE MATCH CAMERA SCREEN (MAIN CAMERA FEATURE)
@SuppressLint("ClickableViewAccessibility")
@Composable
fun PoseCameraScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onGoHome: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val selectedPose by viewModel.selectedPose.collectAsState()
    val matchState by viewModel.poseMatchState.collectAsState()
    // Reference blueprints for the in-camera horizontal selector.
    val allPoses by viewModel.defaultPoses.collectAsState()

    // Transparency Slider Opacity
    val poseOpacity by viewModel.poseOpacity.collectAsState()

    // Pinch rotation offset parameters of pose
    val poseScale by viewModel.poseScale.collectAsState()
    val poseOffsetX by viewModel.poseOffsetX.collectAsState()
    val poseOffsetY by viewModel.poseOffsetY.collectAsState()
    val poseRotation by viewModel.poseRotation.collectAsState()

    // Interactive drift testing coordinates
    val driftX by viewModel.userDriftX.collectAsState()
    val driftY by viewModel.userDriftY.collectAsState()

    // Controls
    val flashMode by viewModel.cameraFlashMode.collectAsState()
    val facingMode by viewModel.cameraFacing.collectAsState()
    val timerSeconds by viewModel.cameraTimer.collectAsState()
    val gridVisible by viewModel.cameraGridVisible.collectAsState()
    val proIso by viewModel.proIso.collectAsState()
    val proExposure by viewModel.proExposure.collectAsState()

    // Auto look extracted from the overlay (used later on the post-capture review screen).
    val autoFilterMatrix by viewModel.autoFilterMatrix.collectAsState()
    val autoFilterColor by viewModel.autoFilterColor.collectAsState()

    // Captured photo pending review (filters + save happen there, not on the live feed).
    var reviewPhotoPath by remember { mutableStateOf<String?>(null) }
    // Saved photo shown on the post-save success screen (Home / Share).
    var savedPhotoPath by remember { mutableStateOf<String?>(null) }

    // Background-removed reference silhouette (only the subject overlays the camera).
    var overlayBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    // Size of the live camera region — used to convert overlay pan into a screen fraction.
    var overlaySize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

    var showProPanel by remember { mutableStateOf(false) }
    var activeTimerCountdown by remember { mutableStateOf(-1) }
    val scope = rememberCoroutineScope()

    // First-run camera coach card — persisted so it appears only once.
    val uiPrefs = remember { context.getSharedPreferences("posematch_ui", Context.MODE_PRIVATE) }
    var showCoach by remember { mutableStateOf(!uiPrefs.getBoolean("camera_coach_seen", false)) }

    // "Great match!" celebration flash when the score first crosses the success threshold.
    var showGreatFlash by remember { mutableStateOf(false) }
    var wasGreat by remember { mutableStateOf(false) }

    // Base camera provider
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val poseProcessor = remember { PoseDetectorProcessor() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    val score = matchState.similarityScore

    LaunchedEffect(score) {
        if (score >= 80) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        150,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(150)
            }
        }
    }

    // Request permissions launcher
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    // Location permission — used to tag captures with a place name / album folder (optional).
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* result ignored — capture still works, just as "Unknown Location" */ }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(android.Manifest.permission.CAMERA)
        }
        val hasLoc = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!hasLoc) {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableEffect(cameraProviderFuture) {
        onDispose {
            try {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
                cameraExecutor.shutdown()
            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }
    }

    // Extract the "Auto" look from the current reference overlay whenever it changes.
    LaunchedEffect(selectedPose?.image) {
        val pose = selectedPose
        if (pose != null && pose.image.isNotEmpty()) {
            val result = withContext(Dispatchers.IO) {
                val bmp = loadOverlayBitmap(context, pose.image)
                bmp?.let { PhotoFilters.extractLook(it) }
            }
            if (result != null) {
                viewModel.setAutoFilter(result.first, result.second)
            }
        }
    }

    // Strip the reference background so only the pose subject overlays the live camera.
    LaunchedEffect(selectedPose?.image) {
        overlayBitmap = null
        val pose = selectedPose
        if (pose != null && pose.image.isNotEmpty()) {
            val extracted = withContext(Dispatchers.IO) {
                val bmp =
                    loadOverlayBitmap(context, pose.image, size = 720) ?: return@withContext null
                SubjectExtractor.removeBackground(bmp)
            }
            overlayBitmap = extracted?.asImageBitmap()
        }
    }

    // Detect the reference pose from the actual photo so matching reflects the real pose
    // (not the generic JSON template). Falls back to the template if detection finds nothing.
    LaunchedEffect(selectedPose?.image) {
        viewModel.setReferenceLandmarks(emptyMap())
        val pose = selectedPose ?: return@LaunchedEffect
        if (pose.image.isEmpty()) return@LaunchedEffect
        val landmarks = withContext(Dispatchers.IO) {
            val bmp = loadOverlayBitmap(context, pose.image, size = 512) ?: return@withContext emptyMap()
            poseProcessor.detectBitmap(bmp)
        }
        if (landmarks.isNotEmpty()) viewModel.setReferenceLandmarks(landmarks)
    }

    // Capture → hand the raw frame to the review screen (filters + save happen there).
    fun triggerPhotoCapture() {
        if (activeTimerCountdown > 0) return
        if (timerSeconds > 0 && activeTimerCountdown == -1) {
            scope.launch {
                activeTimerCountdown = timerSeconds
                while (activeTimerCountdown > 0) {
                    delay(1000)
                    activeTimerCountdown--
                }
                activeTimerCountdown = -1
                takePhotoToFile(
                    context,
                    imageCapture,
                    cameraExecutor,
                    flashMode
                ) { path -> reviewPhotoPath = path }
            }
        } else {
            takePhotoToFile(
                context,
                imageCapture,
                cameraExecutor,
                flashMode
            ) { path -> reviewPhotoPath = path }
        }
    }

    // Success moment — buzz + flash the first time the score reaches a great match.
    LaunchedEffect(matchState.similarityScore) {
        val great = matchState.similarityScore >= 85
        if (great && !wasGreat) {
            runCatching { vibrator.vibrate(120) }
            showGreatFlash = true
        }
        wasGreat = great
    }
    LaunchedEffect(showGreatFlash) {
        if (showGreatFlash) {
            kotlinx.coroutines.delay(1300)
            showGreatFlash = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // TOP pro camera bar — snug to the status bar (top gap matches bottom gap).
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .statusBarsPadding()
                    .height(44.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back to blueprint home
                IconButton(onClick = onBack, modifier = Modifier.testTag("camera_top_back")) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Minimal right cluster — everyday flash + a single "Controls" toggle that
                // reveals grid/timer/ISO/EV. Keeps the default screen uncluttered for beginners.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.toggleFlash() }) {
                        Icon(
                            imageVector = when (flashMode) {
                                "On" -> Icons.Default.FlashOn
                                "Auto" -> Icons.Default.FlashAuto
                                else -> Icons.Default.FlashOff
                            },
                            contentDescription = "Flash",
                            tint = if (flashMode != "Off") AccentCopper else Color.White
                        )
                    }
                    IconButton(onClick = { showProPanel = !showProPanel }) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Camera controls",
                            tint = if (showProPanel || gridVisible || timerSeconds > 0) AccentCopper else Color.White
                        )
                    }
                }
            }

            // Live camera region
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.DarkGray)
                    .onSizeChanged { overlaySize = it }
                    // Pinch to zoom, drag to move, two-finger twist to rotate the pose overlay.
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, rotation ->
                            val w = if (overlaySize.width > 0) overlaySize.width.toFloat() else 1f
                            val h = if (overlaySize.height > 0) overlaySize.height.toFloat() else 1f
                            viewModel.updatePoseGestures(
                                scale = viewModel.poseScale.value * zoom,
                                offsetX = viewModel.poseOffsetX.value + pan.x / w,
                                offsetY = viewModel.poseOffsetY.value + pan.y / h,
                                rotation = viewModel.poseRotation.value + rotation
                            )
                        }
                    }
            ) {
                // Camera preview rendering (Fallback to studio illustration on headless/permission denied models)
                if (hasCameraPermission) {
                    // (Re)bind camera use cases whenever the lens is switched so front/back toggle works.
                    LaunchedEffect(facingMode) {
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val rotation = previewView.display?.rotation ?: Surface.ROTATION_0
                            val preview = Preview.Builder()
                                .setTargetRotation(rotation)
                                .build()
                                .also { it.setSurfaceProvider(previewView.surfaceProvider) }
                            imageCapture.targetRotation = rotation
                            val analysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also { a ->
                                    a.setAnalyzer(cameraExecutor) { imageProxy ->
                                        poseProcessor.processImageProxy(imageProxy, isFrontCamera = facingMode != "Back") { landmarks ->
                                            viewModel.updateDetectedPose(landmarks)
                                        }
                                    }
                                }
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                if (facingMode == "Back") CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA,
                                preview,
                                imageCapture,
                                analysis
                            )
                        } catch (exc: Exception) {
                            exc.printStackTrace()
                        }
                    }

                    AndroidView(
                        factory = {
                            // WYSIWYG framing: fill+center-crop matches the cropped reference overlay.
                            previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
                            previewView.implementationMode =
                                PreviewView.ImplementationMode.COMPATIBLE
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Symmetrical Studio live representation
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF2C1919), Color(0xFF191F2C))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.NoPhotography,
                                contentDescription = "Camera simulated mode",
                                tint = Color.LightGray,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.camera_sim_title),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.camera_sim_hint),
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Grid lines overlay
                if (gridVisible) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 1.5f
                        val col = Color.White.copy(alpha = 0.35f)
                        // verticals
                        drawLine(
                            col,
                            Offset(size.width / 3f, 0f),
                            Offset(size.width / 3f, size.height),
                            stroke
                        )
                        drawLine(
                            col,
                            Offset(size.width * 2f / 3f, 0f),
                            Offset(size.width * 2f / 3f, size.height),
                            stroke
                        )
                        // horizontals
                        drawLine(
                            col,
                            Offset(0f, size.height / 3f),
                            Offset(size.width, size.height / 3f),
                            stroke
                        )
                        drawLine(
                            col,
                            Offset(0f, size.height * 2f / 3f),
                            Offset(size.width, size.height * 2f / 3f),
                            stroke
                        )
                    }
                }

                // 1. DYNAMIC REFERENCE OVERLAY — background stripped, only the pose subject shows.
                selectedPose?.let { pose ->
                    val extracted = overlayBitmap
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = poseScale
                                scaleY = poseScale
                                translationX = poseOffsetX * overlaySize.width
                                translationY = poseOffsetY * overlaySize.height
                                rotationZ = poseRotation
                            }
                    ) {
                    if (extracted != null) {
                        // Whole subject centered (Fit) so the user can mirror the full pose.
                        Image(
                            bitmap = extracted,
                            contentDescription = "Pose Overlay",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            alpha = poseOpacity
                        )
                    } else if (pose.image.isNotEmpty()) {
                        // Fallback while segmenting (or if it fails): the raw reference.
                        val assetPath = assetPathOf(pose.image)
                        if (assetPath != null) {
                            AssetImage(
                                assetPath = assetPath,
                                contentDescription = "Pose Overlay",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                alpha = poseOpacity
                            )
                        } else if (pose.image.startsWith("http") || pose.image.contains("/")) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(pose.image)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Pose Overlay",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                alpha = poseOpacity
                            )
                        } else {
                            val drawableId = context.resources.getIdentifier(
                                pose.image,
                                "drawable",
                                context.packageName
                            )
                            if (drawableId != 0) {
                                Image(
                                    painter = painterResource(id = drawableId),
                                    contentDescription = "Pose Overlay",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit,
                                    alpha = poseOpacity
                                )
                            }
                        }
                    }
                    }
                }


                // Match feedback — score badge + a plain-language coaching label so users
                // always know what to do (fixes the "what does this % mean?" confusion).
                if (selectedPose != null) {
                    val score = matchState.similarityScore
                    val colorGroup = when {
                        score >= 80 -> Color(0xFF2E7D32) // green
                        score >= 60 -> Color(0xFFE0A900) // yellow/orange
                        else -> Color(0xFFC62828) // red
                    }
                    val statusLabel = when {
                        score >= 80 -> "Perfect pose — hold it!"
                        score >= 60 -> "Almost there…"
                        score >= 30 -> "Keep adjusting your pose"
                        score > 0 -> "Match the pose to raise your score"
                        else -> "Move into frame to match"
                    }
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Always show the real percentage — including 0% when no pose is detected.
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(colorGroup.copy(alpha = 0.9f))
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                                .testTag("match_score_badge")
                        ) {
                            Text(
                                text = "Pose Match $score%",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        // Coaching line — always visible while a pose is active.
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Black.copy(alpha = 0.55f))
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = statusLabel,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Live active countdown timer overlay
                if (activeTimerCountdown > 0) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$activeTimerCountdown",
                            fontSize = 120.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentCopper,
                            modifier = Modifier.alpha(0.85f)
                        )
                    }
                }

            }

            // Transparency opacity controller slider block
            selectedPose?.let {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.camera_opacity),
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            "${(poseOpacity * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                    Slider(
                        value = poseOpacity,
                        onValueChange = { viewModel.updatePoseOpacity(it) },
                        colors = SliderDefaults.colors(
                            thumbColor = AccentCopper,
                            activeTrackColor = AccentCopper,
                            inactiveTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.height(18.dp)
                    )
                }
            }

            // Pro Adjustments Panel (Collapsible slide-up)
            if (showProPanel) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftCardGray)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        stringResource(R.string.camera_pro_title),
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Quick toggles: Grid + Timer (moved out of the top bar to declutter it).
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (gridVisible) AccentCopper.copy(alpha = 0.22f) else Color(0xFF1E1E24))
                                .clickable { viewModel.toggleGridVisible() }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.GridOn, contentDescription = null, tint = if (gridVisible) AccentCopper else Color.Gray, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Grid", color = if (gridVisible) AccentCopper else Color.White, fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (timerSeconds > 0) AccentCopper.copy(alpha = 0.22f) else Color(0xFF1E1E24))
                                .clickable {
                                    val next = when (timerSeconds) { 0 -> 3; 3 -> 5; else -> 0 }
                                    viewModel.setTimer(next)
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = if (timerSeconds > 0) AccentCopper else Color.Gray, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (timerSeconds > 0) "Timer ${timerSeconds}s" else "Timer", color = if (timerSeconds > 0) AccentCopper else Color.White, fontSize = 12.sp)
                        }
                    }

                    // ISO slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "ISO",
                            color = Color.Gray,
                            modifier = Modifier.width(48.dp),
                            fontSize = 11.sp
                        )
                        Slider(
                            value = proIso.toFloat(),
                            onValueChange = { viewModel.setProIso(it.roundToInt()) },
                            valueRange = 100f..3200f,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentCopper,
                                activeTrackColor = AccentCopper
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "$proIso",
                            color = Color.White,
                            modifier = Modifier.width(36.dp),
                            fontSize = 11.sp,
                            textAlign = TextAlign.End
                        )
                    }

                    // Exposure slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "EV",
                            color = Color.Gray,
                            modifier = Modifier.width(48.dp),
                            fontSize = 11.sp
                        )
                        Slider(
                            value = proExposure,
                            onValueChange = { viewModel.setProExposure(it) },
                            valueRange = -2.0f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentCopper,
                                activeTrackColor = AccentCopper
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            String.format("%.1f", proExposure),
                            color = Color.White,
                            modifier = Modifier.width(36.dp),
                            fontSize = 11.sp,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            // Reference selector — horizontal recycler of blueprints. Tap one to overlay it live.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(top = 6.dp, bottom = 4.dp)
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 4.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allPoses) { pose ->
                        val isSelected = selectedPose?.id == pose.id
                        val context = LocalContext.current
                        Box(
                            modifier = Modifier
                                .size(width = 46.dp, height = 62.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF14141A))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AccentCopper else GlassWhite,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.selectPose(pose) }
                                .testTag("ref_pose_${pose.id}")
                        ) {
                            if (pose.image.isNotEmpty()) {
                                val assetPath = assetPathOf(pose.image)
                                when {
                                    assetPath != null -> AssetImage(
                                        assetPath = assetPath,
                                        contentDescription = pose.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    pose.image.startsWith("http") || pose.image.contains("/") -> AsyncImage(
                                        model = pose.image,
                                        contentDescription = pose.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    else -> {
                                        val drawableId = context.resources.getIdentifier(
                                            pose.image,
                                            "drawable",
                                            context.packageName
                                        )
                                        if (drawableId != 0) {
                                            Image(
                                                painter = painterResource(id = drawableId),
                                                contentDescription = pose.title,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = AccentCopper,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            // BOTTOM Control dock bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(100.dp)
                    .background(Color.Black)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left placeholder keeps the shutter visually centered.
                Spacer(modifier = Modifier.size(48.dp))

                // Giant Snap Circle Button
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(6.dp)
                        .clickable { triggerPhotoCapture() }
                        .testTag("capture_photo_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(3.dp, Color.Black, CircleShape)
                            .background(Color.White)
                    )
                }

                // Camera toggle (Lens front/back)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SoftCardGray)
                        .clickable { viewModel.toggleCameraFacing() }
                        .testTag("camera_switch_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FlipCameraAndroid,
                        contentDescription = "Toggle Facing",
                        tint = Color.White
                    )
                }
            }
        }

        // Post-capture editor — filters + adjustments, then Save (bakes, saves & returns home).
        // Single-step: the editor already shows a live preview, so no separate preview screen.
        AnimatedVisibility(
            visible = reviewPhotoPath != null,
            enter = fadeIn(tween(220)) + slideInVertically(tween(280)) { it / 6 },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(240)) { it / 6 }
        ) {
            val path = reviewPhotoPath
            if (path != null) {
                PhotoEditScreen(
                    photoPath = path,
                    autoFilterMatrix = autoFilterMatrix,
                    autoFilterColor = autoFilterColor,
                    onDiscard = {
                        runCatching { File(path).delete() }
                        reviewPhotoPath = null
                    },
                    onDone = { result ->
                        // Bake filter + rotate + crop into the file, then show the success screen.
                        savePhoto(
                            context, File(path), result.matrix,
                            selectedPose?.title ?: "Pose Frame", viewModel,
                            result.rotationDeg, result.cropAspect
                        )
                        savedPhotoPath = path
                        reviewPhotoPath = null
                    }
                )
            }
        }

        // Post-save success screen — saved image + success banner + Home / Share.
        AnimatedVisibility(
            visible = savedPhotoPath != null,
            enter = fadeIn(tween(220)) + slideInVertically(tween(280)) { it / 6 },
            exit = fadeOut(tween(180))
        ) {
            val path = savedPhotoPath
            if (path != null) {
                PhotoSuccessScreen(
                    photoPath = path,
                    onHome = {
                        savedPhotoPath = null
                        onGoHome()
                    },
                    onShare = {
                        runCatching {
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context, "${context.packageName}.fileprovider", File(path)
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/*"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(
                                Intent.createChooser(intent, context.getString(R.string.share_frame))
                            )
                        }
                    }
                )
            }
        }

        // Success celebration — brief centered flash when the pose is a great match.
        androidx.compose.animation.AnimatedVisibility(
            visible = showGreatFlash,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(300)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF2E7D32).copy(alpha = 0.92f))
                    .padding(horizontal = 22.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Great match!", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        // First-run coaching card — explains the 3-step loop, shown once.
        if (showCoach) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.78f))
                    .clickable {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(28.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF161619))
                        .padding(22.dp)
                ) {
                    Text("How Pose Match works", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(18.dp))
                    CoachStep(Icons.Default.Collections, "Pick a pose", "Choose one from the strip at the bottom.")
                    Spacer(modifier = Modifier.height(14.dp))
                    CoachStep(Icons.Default.Accessibility, "Match the outline", "Move until your body fills the outline — your score rises.")
                    Spacer(modifier = Modifier.height(14.dp))
                    CoachStep(Icons.Default.CameraAlt, "Snap & save", "Tap the shutter, add a filter, and save it.")
                    Spacer(modifier = Modifier.height(22.dp))
                    Button(
                        onClick = {
                            uiPrefs.edit().putBoolean("camera_coach_seen", true).apply()
                            showCoach = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCopper),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("coach_got_it")
                    ) {
                        Text("Got it", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        BackHandler(enabled = reviewPhotoPath != null || savedPhotoPath != null) {
            when {
                // Back on the success screen → go home (photo is already saved).
                savedPhotoPath != null -> {
                    savedPhotoPath = null
                    onGoHome()
                }
                else -> {
                    reviewPhotoPath?.let { runCatching { File(it).delete() } }
                    reviewPhotoPath = null
                }
            }
        }
    }
}

// One row in the first-run coaching card: number-style icon + title + one-line description.
@Composable
private fun CoachStep(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AccentCopper.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AccentCopper, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, color = Color.Gray, fontSize = 12.sp, lineHeight = 15.sp)
        }
    }
}
