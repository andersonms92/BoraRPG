package com.borarpg.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.borarpg.domain.rules.combat.Combat

@Entity(tableName = "hero")
internal data class HeroEntity(
    @PrimaryKey val id: Int = 1,
    val xp: Int = 0,
    val gold: Int = 0,
    val hp: Int = Combat.GUARDIAN_HEALTH,
    val victories: Int = 0,
    val steps: Int = 0,
)
