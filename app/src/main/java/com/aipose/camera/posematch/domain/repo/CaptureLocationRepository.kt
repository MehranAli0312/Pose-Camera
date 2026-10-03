package com.aipose.camera.posematch.domain.repo

import com.aipose.camera.posematch.domain.models.CaptureLocation

interface CaptureLocationRepository {

    suspend fun resolveCurrentPlace(): CaptureLocation
}
