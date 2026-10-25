package com.borarpg.domain.model.combat

data class GuardianDefeat(
    val victories: Int,
    val totalVictories: Int,
    val bonusXp: Int,
    val gold: Int,
)
