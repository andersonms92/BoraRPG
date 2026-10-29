package com.borarpg.domain.usecase

import com.borarpg.domain.model.mission.QuestPoint
import com.borarpg.domain.model.player.PlayerProfile
import com.borarpg.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow

internal class RecordingPlayerRepository : PlayerRepository {
    val profiles = mutableListOf<PlayerProfile>()
    val savedQuests = mutableListOf<QuestPoint>()

    override val profile: Flow<PlayerProfile>
        get() = error("Profile observation was not expected")

    override val quests: Flow<List<QuestPoint>>
        get() = error("Quest observation was not expected")

    override suspend fun save(profile: PlayerProfile) {
        profiles += profile
    }

    override suspend fun addQuest(quest: QuestPoint) {
        savedQuests += quest
    }

    override suspend fun deleteQuest(id: String) = error("Quest deletion was not expected")
}
