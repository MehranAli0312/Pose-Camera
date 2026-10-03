package com.aipose.camera.posematch.ui.screens.camera.components

import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aipose.camera.posematch.ui.screens.camera.models.CameraFacing
import com.aipose.camera.posematch.ui.screens.camera.models.FlashMode
import java.io.File
import java.util.concurrent.Executors

@Composable
internal fun CameraPreviewSurface(
    facing: CameraFacing,
    flashMode: FlashMode,
    frameAnalyzer: ImageAnalysis.Analyzer,
    captureTarget: String?,
    onCaptured: (String) -> Unit,
    onCaptureFailed: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val currentOnCaptured by rememberUpdatedState(onCaptured)
    val currentOnCaptureFailed by rememberUpdatedState(onCaptureFailed)

    LaunchedEffect(facing) {
        runCatching {
            val cameraProvider = cameraProviderFuture.get()
            val rotation = previewView.display?.rotation ?: Surface.ROTATION_0
            val preview = Preview.Builder()
                .setTargetRotation(rotation)
                .build()
                .also { it.surfaceProvider = previewView.surfaceProvider }
            imageCapture.targetRotation = rotation
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(cameraExecutor, frameAnalyzer) }
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                if (facing == CameraFacing.Back) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                },
                preview,
                imageCapture,
                analysis,
            )
        }
    }

    LaunchedEffect(captureTarget) {
        val target = captureTarget ?: return@LaunchedEffect
        imageCapture.flashMode = when (flashMode) {
            FlashMode.On -> ImageCapture.FLASH_MODE_ON
            FlashMode.Auto -> ImageCapture.FLASH_MODE_AUTO
            FlashMode.Off -> ImageCapture.FLASH_MODE_OFF
        }
        val options = ImageCapture.OutputFileOptions.Builder(File(target)).build()
        imageCapture.takePicture(
            options,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    currentOnCaptured(target)
                }

                override fun onError(exception: ImageCaptureException) {
                    currentOnCaptureFailed(exception.message)
                }
            },
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching {
                if (cameraProviderFuture.isDone) cameraProviderFuture.get().unbindAll()
            }
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = {
            previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
            previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            previewView
        },
        modifier = modifier,
    )
}
