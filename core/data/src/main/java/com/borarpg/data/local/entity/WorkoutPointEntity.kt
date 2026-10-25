package com.borarpg.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey

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
)
