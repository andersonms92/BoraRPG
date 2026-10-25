package com.borarpg.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.borarpg.data.local.entity.WorkoutEntity
import com.borarpg.data.local.entity.WorkoutPointEntity
import kotlinx.coroutines.flow.Flow

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
