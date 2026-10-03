package com.aipose.camera.posematch.data.repoImpl

import com.aipose.camera.posematch.data.local.DeviceLocationDataSource
import com.aipose.camera.posematch.domain.models.CaptureLocation
import com.aipose.camera.posematch.domain.repo.CaptureLocationRepository

class CaptureLocationRepositoryImpl(
    private val locationDataSource: DeviceLocationDataSource
) : CaptureLocationRepository {

    override suspend fun resolveCurrentPlace(): CaptureLocation = locationDataSource.resolvePlace()
}
