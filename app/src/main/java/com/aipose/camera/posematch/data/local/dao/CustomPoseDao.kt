package com.aipose.camera.posematch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aipose.camera.posematch.data.local.entity.CustomPoseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomPoseDao {

    @Query("SELECT * FROM custom_poses ORDER BY id DESC")
    fun observeAll(): Flow<List<CustomPoseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pose: CustomPoseEntity): Long

    @Query("DELETE FROM custom_poses WHERE id = :id")
    suspend fun deleteById(id: Long)
}
