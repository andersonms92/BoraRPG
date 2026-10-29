package com.borarpg.data.repository

import androidx.room.withTransaction
import com.borarpg.data.local.database.GameDatabase
import com.borarpg.data.local.entity.HeroEntity
import com.borarpg.data.local.entity.ItemEntity
import com.borarpg.data.mapper.toDomain
import com.borarpg.data.mapper.toEntity
import com.borarpg.domain.model.mission.MissionProgress
import com.borarpg.domain.model.mission.QuestPoint
import com.borarpg.domain.repository.MissionRepository
import com.borarpg.domain.rules.mission.MissionRules
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@Singleton
internal class MissionRepositoryImpl @Inject constructor(private val db: GameDatabase) : MissionRepository {
    private val dao = db.missionDao()
    override val active =
        combine(dao.observeActive(), db.heroDao().observe()) { mission, hero ->
            mission?.let { MissionProgress(it.toDomain(), it.baselineSteps, hero?.steps ?: 0) }
        }
    override val inventory = dao.inventory().map { items -> items.map { it.toDomain() } }
    override val claimed = dao.claimed().map { it.toSet() }

    override suspend fun accept(quest: QuestPoint) = db.withTransaction {
        MissionRules.validate(quest)
        check(dao.active() == null) { "Conclua ou cancele a missão ativa primeiro." }
        check(dao.find(quest.id)?.status != "claimed") { "Esta recompensa já foi resgatada." }
        val hero = db.heroDao().get() ?: HeroEntity()
        dao.save(quest.toEntity(hero.steps))
    }

    override suspend fun claim() = db.withTransaction {
        val mission = checkNotNull(dao.active()) { "Não há missão ativa para resgatar." }
        val hero = db.heroDao().get() ?: HeroEntity()
        val reward =
            MissionRules.reward(
                hero.toDomain(),
                MissionProgress(mission.toDomain(), mission.baselineSteps, hero.steps),
            )
        db.heroDao().save(reward.toEntity())
        val quantity = dao.item(mission.itemReward)?.quantity ?: 0
        dao.saveItem(ItemEntity(mission.itemReward, quantity + 1))
        dao.save(mission.copy(status = "claimed"))
    }

    override suspend fun cancel(): Unit = db.withTransaction {
        dao.active()?.let { dao.save(it.copy(status = "cancelled")) }
    }
}
