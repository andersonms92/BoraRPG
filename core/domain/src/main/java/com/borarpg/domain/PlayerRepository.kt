package com.borarpg.domain

import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    val profile: Flow<PlayerProfile>
    val quests: Flow<List<QuestPoint>>

    suspend fun save(profile: PlayerProfile)

    suspend fun addQuest(quest: QuestPoint)

    suspend fun deleteQuest(id: String)
}
