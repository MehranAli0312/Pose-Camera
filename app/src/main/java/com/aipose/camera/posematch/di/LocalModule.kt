package com.aipose.camera.posematch.di

import androidx.room.Room
import com.aipose.camera.posematch.data.local.AdsRemoteDataStore
import com.aipose.camera.posematch.data.local.AppDataStore
import com.aipose.camera.posematch.data.local.AppDatabase
import com.aipose.camera.posematch.data.local.CaptureDraftStore
import com.aipose.camera.posematch.data.local.CaptureFileDataSource
import com.aipose.camera.posematch.data.local.CaptureGalleryDataSource
import com.aipose.camera.posematch.data.local.DeviceLocationDataSource
import com.aipose.camera.posematch.data.local.ImportedPoseFileDataSource
import com.aipose.camera.posematch.data.local.NetworkConnectivityChecker
import com.aipose.camera.posematch.data.local.PoseAssetDataSource
import com.aipose.camera.posematch.data.local.PoseImageDataSource
import com.aipose.camera.posematch.data.pose.CaptureProcessor
import com.aipose.camera.posematch.data.pose.MlKitPoseDetector
import com.aipose.camera.posematch.data.pose.PhotoGradingEngine
import com.aipose.camera.posematch.data.pose.SubjectCutoutDataSource
import com.aipose.camera.posematch.domain.models.ProPlan
import com.pdfutility.billing.BillingManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val localModule = module {
    single { AdsRemoteDataStore(androidContext()) }
    single {
        BillingManager(androidContext())
            .setSubscriptions(ProPlan.subscriptionProductIds)
    }
    single { AppDataStore(get()) }
    single { NetworkConnectivityChecker(androidContext()) }

    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, AppDatabase.NAME).build()
    }
    single { get<AppDatabase>().captureDao() }
    single { get<AppDatabase>().customPoseDao() }

    single { PoseAssetDataSource(androidContext()) }
    single { ImportedPoseFileDataSource(androidContext()) }
    single { PoseImageDataSource(androidContext()) }
    single { CaptureGalleryDataSource(androidContext()) }
    single { CaptureFileDataSource(androidContext()) }
    single { CaptureDraftStore() }
    single { DeviceLocationDataSource(androidContext()) }
    single { MlKitPoseDetector() }
    single { SubjectCutoutDataSource() }
    single { PhotoGradingEngine() }
    single { CaptureProcessor(get()) }
}
