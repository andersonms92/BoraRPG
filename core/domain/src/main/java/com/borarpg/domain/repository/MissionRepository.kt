package com.borarpg.domain.repository

import com.borarpg.domain.model.inventory.InventoryItem
import com.borarpg.domain.model.mission.MissionProgress
import com.borarpg.domain.model.mission.QuestPoint
import kotlinx.coroutines.flow.Flow

interface MissionRepository {
    val active: Flow<MissionProgress?>
    val inventory: Flow<List<InventoryItem>>
    val claimed: Flow<Set<String>>

    suspend fun accept(quest: QuestPoint)

    suspend fun claim()

    suspend fun cancel()
}
