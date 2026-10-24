package com.borarpg.domain

data class QuestPoint(
    val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val kind: String,
    val generated: Boolean = false,
    val description: String = "",
    val goalSteps: Int = 0,
    val xpReward: Int = 0,
    val goldReward: Int = 0,
    val itemReward: String = "",
    val rarity: String = "Comum",
)
