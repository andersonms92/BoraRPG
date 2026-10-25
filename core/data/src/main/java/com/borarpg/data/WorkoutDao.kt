package com.borarpg.data

import androidx.room.*
import com.borarpg.domain.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val type: String,
    val startedAt: Long,
    val elapsedMillis: Long = 0,
    val distanceMeters: Double = 0.0,
    val weightKg: Double? = null,
    val status: String = "active",
) {
    fun toDomain(route: List<RoutePoint> = emptyList()) =
        WorkoutSession(
            id,
            WorkoutType.valueOf(type),
            startedAt,
            elapsedMillis,
            distanceMeters,
            weightKg,
            WorkoutStatus.valueOf(status.uppercase()),
            route,
        )
}

@Entity(
    tableName = "workout_points",
    primaryKeys = ["workoutId", "position"],
    foreignKeys =
        [
            ForeignKey(
                entity = WorkoutEntity::class,
                parentColumns = ["id"],
                childColumns = ["workoutId"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
)
data class WorkoutPointEntity(
    val workoutId: String,
    val position: Int,
    val latitude: Double,
    val longitude: Double,
    val time: Long,
    val segment: Int,
) {
    fun toDomain() = RoutePoint(latitude, longitude, time, segment)
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY startedAt DESC")
    fun observe(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE status != 'saved' ORDER BY startedAt DESC LIMIT 1")
    suspend fun current(): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE id = :id") suspend fun find(id: String): WorkoutEntity?

    @Query("SELECT * FROM workout_points WHERE workoutId = :id ORDER BY position")
    fun observePoints(id: String): Flow<List<WorkoutPointEntity>>

    @Query("SELECT * FROM workout_points WHERE workoutId = :id ORDER BY position")
    suspend fun points(id: String): List<WorkoutPointEntity>

    @Query("UPDATE workouts SET status = 'saved' WHERE id = :id AND status = 'draft'")
    suspend fun saveResult(id: String): Int

    @Query("DELETE FROM workouts WHERE id = :id AND status IN ('draft', 'paused')")
    suspend fun discard(id: String): Int

    @Upsert suspend fun save(workout: WorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPoints(points: List<WorkoutPointEntity>)

    @Query("DELETE FROM workouts WHERE id = :id") suspend fun delete(id: String)

    @Query("UPDATE workouts SET status = 'paused' WHERE status = 'active'") suspend fun recover()
}
