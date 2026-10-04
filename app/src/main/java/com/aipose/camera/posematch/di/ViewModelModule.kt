package com.aipose.camera.posematch.di

import com.aipose.camera.posematch.data.pose.PoseFrameAnalyzer
import com.aipose.camera.posematch.ui.vm.CameraSettingsViewModel
import com.aipose.camera.posematch.ui.vm.SettingsViewModel
import com.aipose.camera.posematch.ui.vm.CollectionsViewModel
import com.aipose.camera.posematch.ui.vm.HomeViewModel
import com.aipose.camera.posematch.ui.vm.SavedViewModel
import com.aipose.camera.posematch.ui.vm.LanguageViewModel
import com.aipose.camera.posematch.ui.vm.PhotoEditViewModel
import com.aipose.camera.posematch.ui.vm.PoseAlbumViewModel
import com.aipose.camera.posematch.ui.vm.PoseDetailViewModel
import com.aipose.camera.posematch.ui.vm.PoseCameraViewModel
import com.aipose.camera.posematch.ui.vm.ProViewModel
import com.aipose.camera.posematch.ui.vm.RateUsViewModel
import com.aipose.camera.posematch.ui.vm.SplashViewModel
import com.aipose.camera.posematch.ui.vm.ThemeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory { PoseFrameAnalyzer(get()) }

    viewModel { SplashViewModel(get()) }
    single { LanguageViewModel(get()) }
    single { ThemeViewModel(get()) }
    single { RateUsViewModel(get()) }
    viewModel {
        ProViewModel(
            billingManager = get(),
            remoteConfigStore = get(),
            proStatusRefresher = get(),
        )
    }
    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { SavedViewModel(androidContext(), get(), get(), get()) }
    viewModel { PoseAlbumViewModel(get(), get()) }
    viewModel { PoseDetailViewModel(get(), get(), get(), get()) }
    viewModel { CollectionsViewModel(androidContext(), get()) }
    viewModel { PhotoEditViewModel(get(), get(), get()) }
    viewModel { CameraSettingsViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel {
        PoseCameraViewModel(
            poseLibraryUseCase = get(),
            poseMatchUseCase = get(),
            captureUseCase = get(),
            cameraSettingsUseCase = get(),
            photoEditUseCase = get(),
            poseFrameAnalyzer = get()
        )
    }
}
