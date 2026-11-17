package com.aipose.camera.posematch.ui.screens.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import com.aipose.camera.posematch.ui.common.safeBottomSystemBarsPadding
import androidx.compose.foundation.layout.padding
import com.aipose.camera.posematch.ui.common.safeTopSystemBarsPadding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ads.CameraScreenBottom
import com.aipose.camera.posematch.ui.common.rememberCameraPermissionState
import com.aipose.camera.posematch.ui.common.rememberHapticPulse
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.screens.bottomSheet.PremiumFeatureBottomSheet
import com.aipose.camera.posematch.ui.screens.camera.components.CameraGridOverlay
import com.aipose.camera.posematch.ui.screens.camera.components.CameraMatchCard
import com.aipose.camera.posematch.ui.screens.camera.components.CameraPermissionCard
import com.aipose.camera.posematch.ui.screens.camera.components.CameraPreviewSurface
import com.aipose.camera.posematch.ui.screens.camera.components.CameraScrims
import com.aipose.camera.posematch.ui.screens.camera.components.CameraShutterButton
import com.aipose.camera.posematch.ui.screens.camera.components.CameraTopBar
import com.aipose.camera.posematch.ui.screens.camera.components.CaptureCountdown
import com.aipose.camera.posematch.ui.screens.camera.components.GreatMatchBanner
import com.aipose.camera.posematch.ui.screens.camera.components.OverlayOpacitySlider
import com.aipose.camera.posematch.ui.screens.camera.components.PoseOverlayImage
import com.aipose.camera.posematch.ui.screens.camera.components.PoseStrip
import com.aipose.camera.posematch.ui.vm.PoseCameraViewModel
import com.example.common.showToast
import com.example.ads.AdPlacement
import com.example.ads.compose.AdsSlot
import org.koin.androidx.compose.koinViewModel

private const val HIGH_MATCH_SCORE = 80
private const val HIGH_MATCH_PULSE_MILLIS = 150L
private const val GREAT_MATCH_PULSE_MILLIS = 120L
private const val GREAT_MATCH_FADE_IN_MILLIS = 150
private const val GREAT_MATCH_FADE_OUT_MILLIS = 300
private val MaxControlsWidth = 480.dp

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
    val captureFailedMessage = stringResource(R.string.toast_capture_failed)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                    onCaptureFailed = {
                        viewModel.onCaptureFailed()
                        context.showToast(captureFailedMessage)
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            if (uiState.isGridVisible) {
                CameraGridOverlay(modifier = Modifier.fillMaxSize())
            }

            uiState.overlayImagePath?.let { overlayPath ->
                PoseOverlayImage(
                    imagePath = overlayPath,
                    transform = uiState.overlay,
                    surfaceSize = surfaceSize,
                    isMirrored = uiState.cameraFacing.isFront,
                )
            }

            if (uiState.isCountdownRunning) {
                CaptureCountdown(
                    seconds = uiState.countdownSeconds,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        CameraScrims()

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxHeight()
                .widthIn(max = MaxControlsWidth)
                .fillMaxWidth()
                .safeTopSystemBarsPadding()
                .safeBottomSystemBarsPadding()
                .padding(bottom = 10.dp),
        ) {
            CameraTopBar(
                flashMode = uiState.flashMode,
                timer = uiState.timer,
                isTimerMenuVisible = uiState.isTimerMenuVisible,
                isToolActive = uiState::isToolActive,
                onBack = { navController.popBackStack() },
                onToolClick = viewModel::onToolClicked,
                onTimerSelect = viewModel::setTimer,
                onTimerMenuDismiss = viewModel::hideTimerMenu,
                onToggleFacing = viewModel::toggleCameraFacing,
                onToggleFlash = viewModel::toggleFlashMode,
            )
            if (uiState.selectedPose != null) {
                CameraMatchCard(
                    score = uiState.match.score,
                    feedback = uiState.feedback,
                    bestScore = uiState.bestScore,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            CameraShutterButton(
                onClick = viewModel::onShutterClicked,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (uiState.selectedPose != null && uiState.isPoseOverlayEnabled) {
                OverlayOpacitySlider(
                    opacity = uiState.overlay.opacity,
                    onOpacityChange = viewModel::updateOverlayOpacity,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            PoseStrip(
                poses = uiState.poses,
                selectedPoseId = uiState.selectedPose?.id,
                lockedPoseIds = uiState.lockedPoseIds,
                onPoseSelected = viewModel::onPoseTapped,
            )
            Spacer(modifier = Modifier.height(12.dp))
            AdsSlot(placement = AdPlacement.CameraScreenBottom)
        }

        AnimatedVisibility(
            visible = uiState.isGreatMatchVisible,
            enter = fadeIn(tween(GREAT_MATCH_FADE_IN_MILLIS)),
            exit = fadeOut(tween(GREAT_MATCH_FADE_OUT_MILLIS)),
            modifier = Modifier.align(Alignment.Center),
        ) {
            GreatMatchBanner()
        }

        if (!cameraPermission.isGranted && !cameraPermission.isRequesting) {
            CameraPermissionCard(
                isBlocked = cameraPermission.isBlocked,
                onAllow = cameraPermission.request,
                onClose = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    uiState.lockedPose?.let { pose ->
        PremiumFeatureBottomSheet(
            pose = pose,
            onRewardEarned = { viewModel.unlockAfterRewardedAd(pose) },
            onGoPremiumClick = {
                viewModel.dismissLockedPose()
                navController.navigateOnClick(NavRoute.ProScreenRoute.route)
            },
            onDismissRequest = viewModel::dismissLockedPose,
        )
    }
}
