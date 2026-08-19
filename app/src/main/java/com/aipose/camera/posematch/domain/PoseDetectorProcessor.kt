package com.aipose.camera.posematch.domain

import android.graphics.Bitmap
import android.graphics.PointF
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PoseDetectorProcessor {
    private val options = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()

    private val detector = PoseDetection.getClient(options)

    // Separate single-image detector for analysing the static reference photo.
    private val singleImageDetector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
            .build()
    )

    /** Detects the pose in a static reference [bitmap]; returns normalised landmarks (0..1). */
    suspend fun detectBitmap(bitmap: Bitmap): Map<String, PointF> = suspendCoroutine { cont ->
        val image = InputImage.fromBitmap(bitmap, 0)
        singleImageDetector.process(image)
            .addOnSuccessListener { pose ->
                cont.resume(convertPoseToLandmarks(pose, bitmap.width.toFloat(), bitmap.height.toFloat(), false))
            }
            .addOnFailureListener { cont.resume(emptyMap()) }
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    fun processImageProxy(
        imageProxy: ImageProxy,
        isFrontCamera: Boolean = false,
        onPoseDetected: (Map<String, PointF>) -> Unit
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            // Use imageProxy dimensions for normalization.
            // Note: ML Kit InputImage handles rotation, so we normalize by the rotated dimensions.
            val isRotated = imageProxy.imageInfo.rotationDegrees == 90 || imageProxy.imageInfo.rotationDegrees == 270
            val width = if (isRotated) imageProxy.height else imageProxy.width
            val height = if (isRotated) imageProxy.width else imageProxy.height

            detector.process(image)
                .addOnSuccessListener { pose ->
                    // The front-camera preview is mirrored, but ML Kit analyses the raw (un-mirrored)
                    // sensor frame. Flip X so detected landmarks line up with the mirrored preview and
                    // the reference overlay — otherwise the match score never rises on the front lens.
                    val landmarks = convertPoseToLandmarks(pose, width.toFloat(), height.toFloat(), isFrontCamera)
                    onPoseDetected(landmarks)
                }
                .addOnFailureListener { e ->
                    Log.e("PoseDetector", "Pose detection failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun convertPoseToLandmarks(pose: Pose, width: Float, height: Float, flipX: Boolean): Map<String, PointF> {
        val landmarks = mutableMapOf<String, PointF>()

        fun addLandmark(key: String, mlKitKey: Int) {
            val landmark = pose.getPoseLandmark(mlKitKey)
            if (landmark != null && landmark.inFrameLikelihood > 0.5f) {
                val p = landmark.position
                val nx = (p.x / width).let { if (flipX) 1f - it else it }
                landmarks[key] = PointF(nx, p.y / height)
            }
        }
        
        addLandmark("head", PoseLandmark.NOSE)
        addLandmark("left_shoulder", PoseLandmark.LEFT_SHOULDER)
        addLandmark("right_shoulder", PoseLandmark.RIGHT_SHOULDER)
        addLandmark("left_elbow", PoseLandmark.LEFT_ELBOW)
        addLandmark("right_elbow", PoseLandmark.RIGHT_ELBOW)
        addLandmark("left_wrist", PoseLandmark.LEFT_WRIST)
        addLandmark("right_wrist", PoseLandmark.RIGHT_WRIST)
        addLandmark("left_hip", PoseLandmark.LEFT_HIP)
        addLandmark("right_hip", PoseLandmark.RIGHT_HIP)
        addLandmark("left_knee", PoseLandmark.LEFT_KNEE)
        addLandmark("right_knee", PoseLandmark.RIGHT_KNEE)
        addLandmark("left_ankle", PoseLandmark.LEFT_ANKLE)
        addLandmark("right_ankle", PoseLandmark.RIGHT_ANKLE)

        return landmarks
    }
}
