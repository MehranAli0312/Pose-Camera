package com.aipose.camera.posematch.data.pose

import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.aipose.camera.posematch.domain.models.NormalizedPoint
import com.aipose.camera.posematch.domain.models.PoseJoint
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MlKitPoseDetector {

    private val streamDetector by lazy {
        PoseDetection.getClient(
            PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build()
        )
    }

    private val singleImageDetector by lazy {
        PoseDetection.getClient(
            PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
                .build()
        )
    }

    suspend fun detect(bitmap: Bitmap): Map<PoseJoint, NormalizedPoint> =
        suspendCoroutine { continuation ->
            singleImageDetector.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { pose ->
                    continuation.resume(
                        pose.toLandmarks(
                            width = bitmap.width.toFloat(),
                            height = bitmap.height.toFloat(),
                            mirrored = false
                        )
                    )
                }
                .addOnFailureListener { continuation.resume(emptyMap()) }
        }

    @OptIn(ExperimentalGetImage::class)
    fun detectStream(
        imageProxy: ImageProxy,
        isFrontCamera: Boolean,
        onDetected: (Map<PoseJoint, NormalizedPoint>) -> Unit
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val rotation = imageProxy.imageInfo.rotationDegrees
        val isRotated = rotation == QUARTER_TURN || rotation == THREE_QUARTER_TURN
        val width = if (isRotated) imageProxy.height else imageProxy.width
        val height = if (isRotated) imageProxy.width else imageProxy.height

        streamDetector.process(InputImage.fromMediaImage(mediaImage, rotation))
            .addOnSuccessListener { pose ->
                onDetected(pose.toLandmarks(width.toFloat(), height.toFloat(), isFrontCamera))
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun Pose.toLandmarks(
        width: Float,
        height: Float,
        mirrored: Boolean
    ): Map<PoseJoint, NormalizedPoint> = buildMap {
        ML_KIT_JOINTS.forEach { (joint, mlKitLandmarkType) ->
            val landmark = getPoseLandmark(mlKitLandmarkType) ?: return@forEach
            if (landmark.inFrameLikelihood <= MINIMUM_LIKELIHOOD) return@forEach
            val x = landmark.position.x / width
            put(joint, NormalizedPoint(if (mirrored) 1f - x else x, landmark.position.y / height))
        }
    }

    private companion object {
        const val MINIMUM_LIKELIHOOD = 0.5f
        const val QUARTER_TURN = 90
        const val THREE_QUARTER_TURN = 270

        val ML_KIT_JOINTS = mapOf(
            PoseJoint.Head to PoseLandmark.NOSE,
            PoseJoint.LeftShoulder to PoseLandmark.LEFT_SHOULDER,
            PoseJoint.RightShoulder to PoseLandmark.RIGHT_SHOULDER,
            PoseJoint.LeftElbow to PoseLandmark.LEFT_ELBOW,
            PoseJoint.RightElbow to PoseLandmark.RIGHT_ELBOW,
            PoseJoint.LeftWrist to PoseLandmark.LEFT_WRIST,
            PoseJoint.RightWrist to PoseLandmark.RIGHT_WRIST,
            PoseJoint.LeftHip to PoseLandmark.LEFT_HIP,
            PoseJoint.RightHip to PoseLandmark.RIGHT_HIP,
            PoseJoint.LeftKnee to PoseLandmark.LEFT_KNEE,
            PoseJoint.RightKnee to PoseLandmark.RIGHT_KNEE,
            PoseJoint.LeftAnkle to PoseLandmark.LEFT_ANKLE,
            PoseJoint.RightAnkle to PoseLandmark.RIGHT_ANKLE
        )
    }
}
