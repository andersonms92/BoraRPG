package com.borarpg.data

import androidx.room.withTransaction
import com.borarpg.domain.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.map

@Singleton
class RoomAdventureRepository @Inject constructor(private val db: GameDatabase) :
    AdventureRepository {
    override val hero = db.heroDao().observe().map { (it ?: HeroEntity()).toDomain() }

    override suspend fun recordSteps(steps: Int) = db.withTransaction {
        val next = Combat.advance((db.heroDao().get() ?: HeroEntity()).toDomain(), steps)
        db.heroDao()
            .save(
                HeroEntity(
                    xp = next.xp,
                    gold = next.gold,
                    hp = next.hp,
                    victories = next.victories,
                    steps = next.steps,
                )
            )
    }

    private fun HeroEntity.toDomain() = Hero(xp, gold, hp, victories, steps)
}
