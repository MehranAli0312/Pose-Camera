package com.aipose.camera.posematch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_poses")
data class CustomPoseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val imagePath: String,
    val category: String,
    val difficulty: String,
    val description: String,
    val tags: String,
    val landmarksJson: String
)
