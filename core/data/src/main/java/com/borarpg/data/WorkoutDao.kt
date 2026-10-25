package com.borarpg.data

import androidx.room.*
import com.borarpg.domain.*
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val type: String,
    val startedAt: Long,
    val elapsedMillis: Long = 0,
    val distanceMeters: Double = 0.0,
    val weightKg: Double? = null,
    val status: String = "active",
    val routeJson: String = "[]",
) {
    val mode: WorkoutType
        get() = WorkoutType.valueOf(type)

    val calories: Double?
        get() = workoutCalories(mode, weightKg, elapsedMillis, distanceMeters)

    fun points(): List<RoutePoint> {
        val array = JSONArray(routeJson)
        return (0 until array.length()).map { i ->
            val q = array.getJSONArray(i)
            RoutePoint(q.getDouble(0), q.getDouble(1), q.getLong(2), q.getInt(3))
        }
    }
}

fun encodeRoute(points: List<RoutePoint>): String =
    JSONArray()
        .apply {
            points.forEach {
                put(JSONArray().put(it.latitude).put(it.longitude).put(it.time).put(it.segment))
            }
        }
        .toString()

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY startedAt DESC")
    fun observe(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE status != 'saved' ORDER BY startedAt DESC LIMIT 1")
    suspend fun current(): WorkoutEntity?

    @Query("UPDATE workouts SET status = 'saved' WHERE id = :id AND status = 'draft'")
    suspend fun saveResult(id: String)

    @Query("DELETE FROM workouts WHERE id = :id AND status IN ('draft', 'paused')")
    suspend fun discard(id: String)

    @Upsert suspend fun save(workout: WorkoutEntity)

    @Query("DELETE FROM workouts WHERE id = :id") suspend fun delete(id: String)

    @Query("UPDATE workouts SET status = 'paused' WHERE status = 'active'") suspend fun recover()
}
