package com.aipose.camera.posematch.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Captured Photo Record Entity
@Entity(tableName = "captured_photos")
data class CapturedPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imagePath: String,
    val dateTimestamp: Long,
    val poseId: Int?,
    val matchScore: Int,
    val category: String,
    val isFavorite: Boolean = false,
    val title: String = "Untitled Frame",
    val locationName: String = "Unknown Location",
    val latitude: Double? = null,
    val longitude: Double? = null
)

// 2. Custom Imported Pose Entity
@Entity(tableName = "custom_poses")
data class CustomPose(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val imagePath: String,
    val category: String = "Custom",
    val difficulty: String = "Medium",
    val description: String = "Imported from gallery / external source.",
    val tags: String = "custom,imported",
    val landmarksJson: String // Landmarks array serialized
)

// 3. Room DAO
@Dao
interface AppDao {
    // Captured photos queries
    @Query("SELECT * FROM captured_photos ORDER BY dateTimestamp DESC")
    fun getAllHistory(): Flow<List<CapturedPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCapturedPhoto(photo: CapturedPhoto): Long

    @Query("DELETE FROM captured_photos WHERE id = :id")
    suspend fun deleteCapturedPhoto(id: Long)

    @Query("UPDATE captured_photos SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavoritePhoto(id: Long, isFav: Boolean)

    // Custom poses queries
    @Query("SELECT * FROM custom_poses ORDER BY id DESC")
    fun getAllCustomPoses(): Flow<List<CustomPose>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomPose(pose: CustomPose): Long

    @Query("DELETE FROM custom_poses WHERE id = :id")
    suspend fun deleteCustomPose(id: Long)

    @Query("UPDATE custom_poses SET title = :newTitle WHERE id = :id")
    suspend fun renameCustomPose(id: Long, newTitle: String)
}

// 4. Room Database
@Database(
    entities = [CapturedPhoto::class, CustomPose::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pose_match_camera_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
