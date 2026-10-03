package com.aipose.camera.posematch.data.local.mapper

import com.aipose.camera.posematch.data.local.entity.CapturedPhotoEntity
import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.CaptureLocation

fun CapturedPhotoEntity.toDomain(): Capture = Capture(
    id = id,
    imagePath = imagePath,
    capturedAtMillis = dateTimestamp,
    poseId = poseId,
    matchScore = matchScore,
    category = category,
    title = title,
    isFavorite = isFavorite,
    location = CaptureLocation(
        name = locationName.ifBlank { null },
        latitude = latitude,
        longitude = longitude
    )
)

fun Capture.toEntity(): CapturedPhotoEntity = CapturedPhotoEntity(
    id = id,
    imagePath = imagePath,
    dateTimestamp = capturedAtMillis,
    poseId = poseId,
    matchScore = matchScore,
    category = category,
    isFavorite = isFavorite,
    title = title,
    locationName = location.name.orEmpty(),
    latitude = location.latitude,
    longitude = location.longitude
)
