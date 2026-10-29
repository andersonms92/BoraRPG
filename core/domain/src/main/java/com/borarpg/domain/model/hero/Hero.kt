package com.borarpg.domain.model.hero

import com.borarpg.domain.rules.combat.Combat
import com.borarpg.domain.rules.progression.LevelProgression

data class Hero(
    val xp: Int = 0,
    val gold: Int = 0,
    val hp: Int = Combat.GUARDIAN_HEALTH,
    val victories: Int = 0,
    val steps: Int = 0,
) {
    private val progression = LevelProgression.fromTotalXp(xp)

    val level: Int
        get() = progression.level

    val xpInLevel: Long
        get() = progression.earnedXp

    val xpForNextLevel: Long
        get() = progression.requiredXp

    val xpToNextLevel: Long
        get() = progression.remainingXp

    val levelProgress: Float
        get() = progression.fraction
}
