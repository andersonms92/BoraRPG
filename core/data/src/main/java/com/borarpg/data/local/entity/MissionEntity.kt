package com.borarpg.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missions")
internal data class MissionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val kind: String,
    val description: String,
    val goalSteps: Int,
    val xpReward: Int,
    val goldReward: Int,
    val itemReward: String,
    val rarity: String,
    val baselineSteps: Int,
    val status: String = "active",
)
