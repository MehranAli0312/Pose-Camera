package com.aipose.camera.posematch.di

import com.aipose.camera.posematch.data.repoImpl.CameraSettingsRepositoryImpl
import com.aipose.camera.posematch.data.repoImpl.CaptureLocationRepositoryImpl
import com.aipose.camera.posematch.data.repoImpl.CaptureRepositoryImpl
import com.aipose.camera.posematch.data.repoImpl.FavoritePoseRepositoryImpl
import com.aipose.camera.posematch.data.repoImpl.LanguageRepositoryImpl
import com.aipose.camera.posematch.data.repoImpl.PhotoGradingRepositoryImpl
import com.aipose.camera.posematch.data.repoImpl.PoseRepositoryImpl
import com.aipose.camera.posematch.data.repoImpl.RateUsRepositoryImpl
import com.aipose.camera.posematch.data.repoImpl.SplashStatusRepositoryImpl
import com.aipose.camera.posematch.domain.repo.CameraSettingsRepository
import com.aipose.camera.posematch.domain.repo.CaptureLocationRepository
import com.aipose.camera.posematch.domain.repo.CaptureRepository
import com.aipose.camera.posematch.domain.repo.FavoritePoseRepository
import com.aipose.camera.posematch.domain.repo.LanguageRepository
import com.aipose.camera.posematch.domain.repo.PhotoGradingRepository
import com.aipose.camera.posematch.domain.repo.PoseRepository
import com.aipose.camera.posematch.domain.repo.RateUsRepository
import com.aipose.camera.posematch.domain.repo.SplashStatusRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single<SplashStatusRepository> { SplashStatusRepositoryImpl(get()) }
    single<LanguageRepository> { LanguageRepositoryImpl(get()) }
    single<FavoritePoseRepository> { FavoritePoseRepositoryImpl(get()) }
    single<RateUsRepository> { RateUsRepositoryImpl(get()) }
    single<CameraSettingsRepository> { CameraSettingsRepositoryImpl(get()) }
    single<PoseRepository> {
        PoseRepositoryImpl(
            context = androidContext(),
            assetDataSource = get(),
            importedFileDataSource = get(),
            imageDataSource = get(),
            shareDataSource = get(),
            customPoseDao = get(),
            poseDetector = get(),
            cutoutDataSource = get()
        )
    }
    single<CaptureRepository> { CaptureRepositoryImpl(get(), get(), get(), get()) }
    single<CaptureLocationRepository> { CaptureLocationRepositoryImpl(get()) }
    single<PhotoGradingRepository> { PhotoGradingRepositoryImpl(get(), get(), get()) }
}
