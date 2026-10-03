package com.aipose.camera.posematch.domain

import android.graphics.PointF
import com.aipose.camera.posematch.data.PoseItem
import kotlin.math.atan2

/** A single on-screen pose point. */
data class SourcedLandmark(
    val name: String,
    val position: PointF,
    val isDetected: Boolean = true
)

/** Live pose match state. */
data class PoseMatchState(
    val similarityScore: Int = 0,
    val userSkeleton: List<SourcedLandmark> = emptyList(),
    val referenceSkeleton: List<SourcedLandmark> = emptyList()
)

object PoseMatchAnalyzer {
    private val LANDMARK_KEYS = listOf(
        "head",
        "left_shoulder", "right_shoulder",
        "left_elbow", "right_elbow",
        "left_wrist", "right_wrist",
        "left_hip", "right_hip",
        "left_knee", "right_knee",
        "left_ankle", "right_ankle"
    )

    // Limb segments whose ORIENTATION defines the pose. Comparing bone angles (not point
    // positions) makes the score invariant to where the person stands, how big they appear,
    // and their body proportions — so a correct pose reliably scores high.
    private val BONES = listOf(
        "left_shoulder" to "left_elbow",
        "left_elbow" to "left_wrist",
        "right_shoulder" to "right_elbow",
        "right_elbow" to "right_wrist",
        "left_hip" to "left_knee",
        "left_knee" to "left_ankle",
        "right_hip" to "right_knee",
        "right_knee" to "right_ankle",
        "left_shoulder" to "left_hip",
        "right_shoulder" to "right_hip"
    )

    // How forgiving the score is. Larger = more forgiving. At DEG_TOLERANCE avg error the
    // score reaches 0; a good match (~10–20° avg error) lands around 75–87%.
    private const val DEG_TOLERANCE = 75f

    /**
     * @param referenceOverride landmarks detected from the actual reference photo. When present
     * (>= 4 points) they replace the JSON template so matching reflects the real pose.
     */
    fun matchPose(
        userPoints: Map<String, PointF>,
        referencePose: PoseItem,
        referenceOverride: Map<String, PointF>? = null
    ): PoseMatchState {
        // Reference landmarks: prefer ones detected from the image, else the JSON template.
        val refPoints = HashMap<String, PointF>()
        if (referenceOverride != null && referenceOverride.size >= 4) {
            refPoints.putAll(referenceOverride)
        } else {
            for (key in LANDMARK_KEYS) {
                val rx = referencePose.landmarks["${key}_x"]
                val ry = referencePose.landmarks["${key}_y"]
                if (rx != null && ry != null) refPoints[key] = PointF(rx, ry)
            }
        }

        val refSke = LANDMARK_KEYS.map { key ->
            val p = refPoints[key]
            if (p != null) SourcedLandmark(key, p) else SourcedLandmark(key, PointF(-1f, -1f), false)
        }
        val userSke = LANDMARK_KEYS.map { key ->
            val p = userPoints[key]
            if (p != null) SourcedLandmark(key, p, true) else SourcedLandmark(key, PointF(-1f, -1f), false)
        }

        var totalDeg = 0f
        var counted = 0
        for ((a, b) in BONES) {
            val ua = userPoints[a]; val ub = userPoints[b]
            val ra = refPoints[a]; val rb = refPoints[b]
            if (ua != null && ub != null && ra != null && rb != null) {
                val userAng = atan2((ub.y - ua.y).toDouble(), (ub.x - ua.x).toDouble())
                val refAng = atan2((rb.y - ra.y).toDouble(), (rb.x - ra.x).toDouble())
                totalDeg += angleDiffDegrees(userAng, refAng)
                counted++
            }
        }

        // Need a few comparable limbs to judge the pose.
        if (counted < 3) return PoseMatchState(0, userSke, refSke)

        val avgDeg = totalDeg / counted
        val score = ((1f - avgDeg / DEG_TOLERANCE) * 100f).toInt().coerceIn(0, 100)

        return PoseMatchState(
            similarityScore = score,
            userSkeleton = userSke,
            referenceSkeleton = refSke
        )
    }

    // Smallest absolute difference between two angles (radians), in degrees, range 0..180.
    private fun angleDiffDegrees(a: Double, b: Double): Float {
        var d = Math.toDegrees(a - b).toFloat()
        d = ((d % 360f) + 360f) % 360f
        if (d > 180f) d = 360f - d
        return d
    }
}
