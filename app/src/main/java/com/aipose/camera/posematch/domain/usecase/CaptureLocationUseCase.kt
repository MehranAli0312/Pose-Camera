package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.models.CaptureLocation
import com.aipose.camera.posematch.domain.repo.CaptureLocationRepository

class CaptureLocationUseCase(
    private val captureLocationRepository: CaptureLocationRepository
) {

    suspend fun resolveCurrentPlace(): CaptureLocation =
        captureLocationRepository.resolveCurrentPlace()
}
