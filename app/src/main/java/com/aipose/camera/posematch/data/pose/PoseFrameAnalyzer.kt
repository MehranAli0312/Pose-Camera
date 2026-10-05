package com.aipose.camera.posematch.data.pose

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.aipose.camera.posematch.domain.models.NormalizedPoint
import com.aipose.camera.posematch.domain.models.PoseJoint

class PoseFrameAnalyzer(private val poseDetector: MlKitPoseDetector) : ImageAnalysis.Analyzer {

    @Volatile
    var onLandmarks: ((Map<PoseJoint, NormalizedPoint>) -> Unit)? = null

    override fun analyze(image: ImageProxy) {
        val listener = onLandmarks
        if (listener == null) {
            image.close()
            return
        }
        poseDetector.detectStream(image, listener)
    }
}
