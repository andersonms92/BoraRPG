package com.borarpg.domain.rules.progression

data class LevelProgress(
    val level: Int,
    val earnedXp: Long,
    val requiredXp: Long,
) {
    val remainingXp: Long
        get() = requiredXp - earnedXp

    val fraction: Float
        get() = earnedXp.toFloat() / requiredXp
}
