package com.borarpg.data

import androidx.room.withTransaction
import com.borarpg.domain.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@Singleton
class MissionRepository @Inject constructor(private val db: GameDatabase) {
    private val dao = db.missionDao()
    val active =
        combine(dao.observeActive(), db.heroDao().observe()) { mission, hero ->
            mission?.let { MissionProgress(it.toQuest(), it.baselineSteps, hero?.steps ?: 0) }
        }
    val inventory =
        dao.inventory().map { items -> items.map { InventoryItem(it.name, it.quantity) } }
    val claimed = dao.claimed().map { it.toSet() }

    suspend fun accept(quest: QuestPoint) = db.withTransaction {
        require(
            quest.generated &&
                quest.goalSteps in 200..5000 &&
                quest.xpReward in 1..1000 &&
                quest.goldReward in 0..1000 &&
                quest.itemReward.isNotBlank()
        ) {
            "Missão inválida."
        }
        check(dao.active() == null) { "Conclua ou cancele a missão ativa primeiro." }
        check(dao.find(quest.id)?.status != "claimed") { "Esta recompensa já foi resgatada." }
        val hero = db.heroDao().get() ?: HeroEntity()
        dao.save(
            MissionEntity(
                quest.id,
                quest.title,
                quest.latitude,
                quest.longitude,
                quest.kind,
                quest.description,
                quest.goalSteps,
                quest.xpReward,
                quest.goldReward,
                quest.itemReward,
                quest.rarity,
                hero.steps,
            )
        )
    }

    suspend fun claim() = db.withTransaction {
        val mission = checkNotNull(dao.active()) { "Não há missão ativa para resgatar." }
        val hero = db.heroDao().get() ?: HeroEntity()
        check(MissionProgress(mission.toQuest(), mission.baselineSteps, hero.steps).ready) {
            "Complete a meta de passos para resgatar."
        }
        db.heroDao()
            .save(hero.copy(xp = hero.xp + mission.xpReward, gold = hero.gold + mission.goldReward))
        val quantity = dao.item(mission.itemReward)?.quantity ?: 0
        dao.saveItem(ItemEntity(mission.itemReward, quantity + 1))
        dao.save(mission.copy(status = "claimed"))
    }

    suspend fun cancel() = db.withTransaction {
        dao.active()?.let { dao.save(it.copy(status = "cancelled")) }
    }

    private fun MissionEntity.toQuest() =
        QuestPoint(
            id,
            title,
            latitude,
            longitude,
            kind,
            true,
            description,
            goalSteps,
            xpReward,
            goldReward,
            itemReward,
            rarity,
        )
}
