package com.borarpg.domain.rules.progression

object LevelProgression {
    private const val INITIAL_REQUIREMENT = 1000L
    private const val REQUIREMENT_INCREASE = 250L

    fun fromTotalXp(totalXp: Int): LevelProgress {
        require(totalXp >= 0) { "XP não pode ser negativo" }
        var lower = 1
        var upper = (totalXp / INITIAL_REQUIREMENT + 1).toInt()
        while (lower < upper) {
            val candidate = lower + (upper - lower + 1) / 2
            if (threshold(candidate) <= totalXp) {
                lower = candidate
            } else {
                upper = candidate - 1
            }
        }
        return LevelProgress(
            level = lower,
            earnedXp = totalXp - threshold(lower),
            requiredXp = INITIAL_REQUIREMENT + REQUIREMENT_INCREASE * (lower - 1L),
        )
    }

    private fun threshold(level: Int): Long {
        val completedLevels = level - 1L
        return INITIAL_REQUIREMENT * completedLevels +
            REQUIREMENT_INCREASE * completedLevels * (completedLevels - 1) / 2
    }
}
