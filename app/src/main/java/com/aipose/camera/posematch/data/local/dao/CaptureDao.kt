package com.aipose.camera.posematch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aipose.camera.posematch.data.local.entity.CapturedPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CaptureDao {

    @Query("SELECT * FROM captured_photos ORDER BY dateTimestamp DESC")
    fun observeAll(): Flow<List<CapturedPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(capture: CapturedPhotoEntity): Long

    @Query("DELETE FROM captured_photos WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE captured_photos SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
}
