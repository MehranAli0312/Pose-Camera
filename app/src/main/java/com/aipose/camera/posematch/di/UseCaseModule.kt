package com.aipose.camera.posematch.di

import com.aipose.camera.posematch.domain.usecase.CameraSettingsUseCase
import com.aipose.camera.posematch.domain.usecase.CaptureLocationUseCase
import com.aipose.camera.posematch.domain.usecase.CaptureProgressUseCase
import com.aipose.camera.posematch.domain.usecase.FavoritePoseUseCase
import com.aipose.camera.posematch.domain.usecase.CaptureUseCase
import com.aipose.camera.posematch.domain.usecase.LanguageUseCase
import com.aipose.camera.posematch.domain.usecase.PhotoEditUseCase
import com.aipose.camera.posematch.domain.usecase.PoseLibraryUseCase
import com.aipose.camera.posematch.domain.usecase.PoseMatchUseCase
import com.aipose.camera.posematch.domain.usecase.RateUsUseCase
import com.aipose.camera.posematch.domain.usecase.SplashStatusUseCase
import com.aipose.camera.posematch.domain.usecase.ThemeUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { SplashStatusUseCase(get()) }
    factory { LanguageUseCase(get()) }
    factory { ThemeUseCase(get()) }
    factory { RateUsUseCase(get()) }
    factory { CameraSettingsUseCase(get()) }
    factory { PoseLibraryUseCase(get()) }
    factory { PoseMatchUseCase() }
    factory { CaptureUseCase(get()) }
    factory { CaptureProgressUseCase() }
    factory { FavoritePoseUseCase(get()) }
    factory { CaptureLocationUseCase(get()) }
    factory { PhotoEditUseCase(get()) }
}
