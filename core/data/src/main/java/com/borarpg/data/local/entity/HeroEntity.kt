package com.borarpg.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hero")
data class HeroEntity(
    @PrimaryKey val id: Int = 1,
    val xp: Int = 0,
    val gold: Int = 0,
    val hp: Int = 1000,
    val victories: Int = 0,
    val steps: Int = 0,
)
