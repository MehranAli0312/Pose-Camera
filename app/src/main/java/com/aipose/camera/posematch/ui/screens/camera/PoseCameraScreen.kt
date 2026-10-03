package com.aipose.camera.posematch.ui.screens.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.rememberCameraPermissionState
import com.aipose.camera.posematch.ui.common.rememberHapticPulse
import com.example.common.showToast
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.screens.camera.components.CameraCoachOverlay
import com.aipose.camera.posematch.ui.screens.camera.components.CameraGridOverlay
import com.aipose.camera.posematch.ui.screens.camera.components.CameraPermissionCard
import com.aipose.camera.posematch.ui.screens.camera.components.CameraPreviewSurface
import com.aipose.camera.posematch.ui.screens.camera.components.CameraShutterBar
import com.aipose.camera.posematch.ui.screens.camera.components.CameraSimulatedSurface
import com.aipose.camera.posematch.ui.screens.camera.components.CameraTopBar
import com.aipose.camera.posematch.ui.screens.camera.components.CaptureCountdown
import com.aipose.camera.posematch.ui.screens.camera.components.GreatMatchBanner
import com.aipose.camera.posematch.ui.screens.camera.components.MatchScoreBanner
import com.aipose.camera.posematch.ui.screens.camera.components.OverlayOpacitySlider
import com.aipose.camera.posematch.ui.screens.camera.components.PoseOverlayImage
import com.aipose.camera.posematch.ui.screens.camera.components.PoseStrip
import com.aipose.camera.posematch.ui.screens.camera.components.ProControlsPanel
import com.aipose.camera.posematch.ui.vm.PoseCameraViewModel
import org.koin.androidx.compose.koinViewModel

private const val HIGH_MATCH_SCORE = 80
private const val HIGH_MATCH_PULSE_MILLIS = 150L
private const val GREAT_MATCH_PULSE_MILLIS = 120L

@Composable
fun PoseCameraScreen(
    navController: NavHostController,
    poseId: Int?,
    viewModel: PoseCameraViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermission = rememberCameraPermissionState()
    val pulse = rememberHapticPulse()
    val context = LocalContext.current
    val captureFailedTemplate = stringResource(R.string.toast_capture_failed)
    var surfaceSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(poseId) {
        if (poseId != null) viewModel.onPoseRequested(poseId)
    }

    LaunchedEffect(uiState.match.score) {
        if (uiState.match.score >= HIGH_MATCH_SCORE) pulse(HIGH_MATCH_PULSE_MILLIS)
    }

    LaunchedEffect(uiState.isGreatMatchVisible) {
        if (uiState.isGreatMatchVisible) pulse(GREAT_MATCH_PULSE_MILLIS)
    }

    LaunchedEffect(uiState.capturedPath) {
        if (uiState.capturedPath != null) {
            viewModel.onCapturedPathHandled()
            navController.navigateOnClick(NavRoute.PhotoEditScreenRoute.route)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            CameraTopBar(
                flashMode = uiState.flashMode,
                areControlsHighlighted = uiState.areProControlsVisible ||
                    uiState.isGridVisible ||
                    uiState.timer.isEnabled,
                onBack = { navController.popBackStack() },
                onToggleFlash = viewModel::toggleFlashMode,
                onToggleControls = viewModel::toggleProControls,
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onSizeChanged { size -> surfaceSize = size }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, rotation ->
                            val width = surfaceSize.width.takeIf { it > 0 }?.toFloat() ?: 1f
                            val height = surfaceSize.height.takeIf { it > 0 }?.toFloat() ?: 1f
                            val overlay = viewModel.uiState.value.overlay
                            viewModel.updateOverlayGestures(
                                scale = overlay.scale * zoom,
                                offsetX = overlay.offsetX + pan.x / width,
                                offsetY = overlay.offsetY + pan.y / height,
                                rotation = overlay.rotation + rotation,
                            )
                        }
                    }
            ) {
                if (cameraPermission.isGranted) {
                    CameraPreviewSurface(
                        facing = uiState.cameraFacing,
                        flashMode = uiState.flashMode,
                        frameAnalyzer = viewModel.frameAnalyzer,
                        captureTarget = uiState.pendingCaptureTarget,
                        onCaptured = viewModel::onCaptureSucceeded,
                        onCaptureFailed = { message ->
                            viewModel.onCaptureFailed()
                            context.showToast(captureFailedTemplate.format(message.orEmpty()))
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    CameraSimulatedSurface(modifier = Modifier.fillMaxSize())
                    CameraPermissionCard(
                        onAllow = cameraPermission.request,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                if (uiState.isGridVisible) {
                    CameraGridOverlay(modifier = Modifier.fillMaxSize())
                }

                val overlayPath = uiState.overlayCutoutPath ?: uiState.selectedPose?.imagePath
                if (overlayPath != null) {
                    PoseOverlayImage(
                        imagePath = overlayPath,
                        transform = uiState.overlay,
                        surfaceSize = surfaceSize,
                    )
                }

                if (uiState.selectedPose != null) {
                    MatchScoreBanner(
                        score = uiState.match.score,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }

                if (uiState.isCountdownRunning) {
                    CaptureCountdown(
                        seconds = uiState.countdownSeconds,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            if (uiState.selectedPose != null) {
                OverlayOpacitySlider(
                    opacity = uiState.overlay.opacity,
                    onOpacityChange = viewModel::updateOverlayOpacity,
                )
            }

            if (uiState.areProControlsVisible) {
                ProControlsPanel(
                    isGridVisible = uiState.isGridVisible,
                    timer = uiState.timer,
                    iso = uiState.manualIso,
                    exposure = uiState.manualExposure,
                    onToggleGrid = viewModel::toggleGrid,
                    onCycleTimer = viewModel::cycleTimer,
                    onIsoChange = viewModel::setManualIso,
                    onExposureChange = viewModel::setManualExposure,
                )
            }

            PoseStrip(
                poses = uiState.poses,
                selectedPoseId = uiState.selectedPose?.id,
                onPoseSelected = viewModel::selectPose,
            )

            CameraShutterBar(
                onShutterClick = viewModel::onShutterClicked,
                onToggleFacing = viewModel::toggleCameraFacing,
            )
        }

        AnimatedVisibility(
            visible = uiState.isGreatMatchVisible,
            enter = fadeIn(tween(GREAT_MATCH_FADE_IN_MILLIS)),
            exit = fadeOut(tween(GREAT_MATCH_FADE_OUT_MILLIS)),
            modifier = Modifier.align(Alignment.Center),
        ) {
            GreatMatchBanner()
        }

        if (uiState.isCoachVisible) {
            CameraCoachOverlay(onDismiss = viewModel::dismissCoach)
        }
    }
}

private const val GREAT_MATCH_FADE_IN_MILLIS = 150
private const val GREAT_MATCH_FADE_OUT_MILLIS = 300
