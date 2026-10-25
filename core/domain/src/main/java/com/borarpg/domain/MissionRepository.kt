package com.borarpg.domain

import kotlinx.coroutines.flow.Flow

interface MissionRepository {
    val active: Flow<MissionProgress?>
    val inventory: Flow<List<InventoryItem>>
    val claimed: Flow<Set<String>>

    suspend fun accept(quest: QuestPoint)

    suspend fun claim()

    suspend fun cancel()
}
