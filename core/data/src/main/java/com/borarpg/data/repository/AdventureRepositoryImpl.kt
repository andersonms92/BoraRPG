package com.borarpg.data.repository

import androidx.room.withTransaction
import com.borarpg.data.local.database.GameDatabase
import com.borarpg.data.local.entity.HeroEntity
import com.borarpg.data.mapper.toDomain
import com.borarpg.data.mapper.toEntity
import com.borarpg.domain.model.combat.GuardianDefeat
import com.borarpg.domain.repository.AdventureRepository
import com.borarpg.domain.rules.combat.Combat
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.map

@Singleton
internal class AdventureRepositoryImpl @Inject constructor(private val db: GameDatabase) :
    AdventureRepository {
    override val hero = db.heroDao().observe().map { (it ?: HeroEntity()).toDomain() }

    override suspend fun recordSteps(steps: Int): GuardianDefeat? = db.withTransaction {
        val previous = (db.heroDao().get() ?: HeroEntity()).toDomain()
        val next = Combat.advance(previous, steps)
        db.heroDao().save(next.toEntity())
        val victories = next.victories - previous.victories
        if (victories > 0)
            GuardianDefeat(
                victories = victories,
                totalVictories = next.victories,
                bonusXp = victories * Combat.VICTORY_XP,
                gold = victories * Combat.VICTORY_GOLD,
            )
        else null
    }
}
