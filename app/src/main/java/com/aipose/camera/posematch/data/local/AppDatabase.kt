package com.aipose.camera.posematch.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aipose.camera.posematch.data.local.dao.CaptureDao
import com.aipose.camera.posematch.data.local.dao.CustomPoseDao
import com.aipose.camera.posematch.data.local.entity.CapturedPhotoEntity
import com.aipose.camera.posematch.data.local.entity.CustomPoseEntity

@Database(
    entities = [CapturedPhotoEntity::class, CustomPoseEntity::class],
    version = AppDatabase.VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun captureDao(): CaptureDao

    abstract fun customPoseDao(): CustomPoseDao

    companion object {
        const val NAME = "pose_match_camera_db"
        const val VERSION = 2
    }
}
