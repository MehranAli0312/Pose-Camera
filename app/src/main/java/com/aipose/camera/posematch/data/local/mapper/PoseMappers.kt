package com.aipose.camera.posematch.data.local.mapper

import com.aipose.camera.posematch.data.local.dto.PoseTemplateDto
import com.aipose.camera.posematch.data.local.entity.CustomPoseEntity
import com.aipose.camera.posematch.domain.models.NormalizedPoint
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.models.PoseJoint
import com.aipose.camera.posematch.domain.models.PoseSource

private const val X_SUFFIX = "_x"
private const val Y_SUFFIX = "_y"

fun Map<String, Float>.toLandmarks(): Map<PoseJoint, NormalizedPoint> =
    PoseJoint.entries.mapNotNull { joint ->
        val x = this[joint.key + X_SUFFIX]
        val y = this[joint.key + Y_SUFFIX]
        if (x == null || y == null) null else joint to NormalizedPoint(x, y)
    }.toMap()

fun Map<PoseJoint, NormalizedPoint>.toFlatLandmarks(): Map<String, Float> =
    buildMap {
        this@toFlatLandmarks.forEach { (joint, point) ->
            put(joint.key + X_SUFFIX, point.x)
            put(joint.key + Y_SUFFIX, point.y)
        }
    }

fun PoseTemplateDto.toDomain(): Pose = Pose(
    id = id,
    title = title,
    category = category,
    description = description,
    difficulty = difficulty,
    tags = tags,
    imagePath = image,
    source = PoseSource.Bundled,
    landmarks = landmarks.toLandmarks()
)

fun CustomPoseEntity.toDomain(landmarks: Map<String, Float>): Pose = Pose(
    id = -id.toInt(),
    title = title,
    category = category,
    description = description,
    difficulty = difficulty,
    tags = tags.split(',').map { it.trim() }.filter { it.isNotEmpty() },
    imagePath = imagePath,
    source = PoseSource.Imported,
    landmarks = landmarks.toLandmarks()
)
