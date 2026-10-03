package com.aipose.camera.posematch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "captured_photos")
data class CapturedPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imagePath: String,
    val dateTimestamp: Long,
    val poseId: Int?,
    val matchScore: Int,
    val category: String,
    val isFavorite: Boolean = false,
    val title: String = "",
    val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)
