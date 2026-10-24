package com.borarpg.domain

data class MissionProgress(val quest: QuestPoint, val baselineSteps: Int, val totalSteps: Int) {
    val steps: Int
        get() = (totalSteps - baselineSteps).coerceIn(0, quest.goalSteps)

    val ready: Boolean
        get() = quest.goalSteps > 0 && steps >= quest.goalSteps

    val fraction: Float
        get() = if (quest.goalSteps > 0) steps.toFloat() / quest.goalSteps else 0f
}

data class InventoryItem(val name: String, val quantity: Int)
