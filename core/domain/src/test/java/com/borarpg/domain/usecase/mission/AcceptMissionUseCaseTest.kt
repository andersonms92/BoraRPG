package com.borarpg.domain.usecase.mission

import com.borarpg.domain.model.inventory.InventoryItem
import com.borarpg.domain.model.mission.MissionProgress
import com.borarpg.domain.model.mission.QuestKind
import com.borarpg.domain.model.mission.QuestPoint
import com.borarpg.domain.repository.MissionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AcceptMissionUseCaseTest {
    private val quest =
        QuestPoint(
            id = "quest",
            title = "Guardião",
            latitude = -23.55,
            longitude = -46.63,
            kind = QuestKind.COMBAT,
            generated = true,
            goalSteps = 1000,
            xpReward = 150,
            goldReward = 60,
            itemReward = "Relíquia de prata",
        )

    @Test
    fun invalidMissionNeverReachesRepository() = runTest {
        val repository = RecordingMissionRepository()
        val accept = AcceptMissionUseCase(repository)
        val invalid =
            listOf(
                quest.copy(generated = false),
                quest.copy(goalSteps = 199),
                quest.copy(xpReward = 0),
                quest.copy(goldReward = -1),
                quest.copy(itemReward = ""),
            )
        for (value in invalid) {
            assertTrue(runCatching { accept(value) }.exceptionOrNull() is IllegalArgumentException)
        }
        assertTrue(repository.accepted.isEmpty())
    }

    @Test
    fun acceptsOnlyValidMissionInASequenceOfRequests() = runTest {
        val repository = RecordingMissionRepository()
        val accept = AcceptMissionUseCase(repository)
        runCatching { accept(quest.copy(generated = false)) }
        accept(quest)
        runCatching { accept(quest.copy(goalSteps = 5001)) }
        assertEquals(listOf(quest), repository.accepted)
    }

    private class RecordingMissionRepository : MissionRepository {
        val accepted = mutableListOf<QuestPoint>()

        override val active: Flow<MissionProgress?>
            get() = error("Mission observation was not expected")

        override val inventory: Flow<List<InventoryItem>>
            get() = error("Inventory observation was not expected")

        override val claimed: Flow<Set<String>>
            get() = error("Claim observation was not expected")

        override suspend fun accept(quest: QuestPoint) {
            accepted += quest
        }

        override suspend fun claim() = error("Reward claim was not expected")

        override suspend fun cancel() = error("Mission cancellation was not expected")
    }
}
