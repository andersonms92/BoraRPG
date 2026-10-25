package com.borarpg.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val type: String,
    val startedAt: Long,
    val elapsedMillis: Long = 0,
    val distanceMeters: Double = 0.0,
    val weightKg: Double? = null,
    val status: String = "active",
)
