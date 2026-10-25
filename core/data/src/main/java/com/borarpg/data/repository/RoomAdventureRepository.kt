package com.borarpg.data.repository

import androidx.room.withTransaction
import com.borarpg.data.local.database.GameDatabase
import com.borarpg.data.local.entity.HeroEntity
import com.borarpg.data.mapper.toDomain
import com.borarpg.data.mapper.toEntity
import com.borarpg.domain.repository.AdventureRepository
import com.borarpg.domain.rules.combat.Combat
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.map

@Singleton
class RoomAdventureRepository @Inject constructor(private val db: GameDatabase) :
    AdventureRepository {
    override val hero = db.heroDao().observe().map { (it ?: HeroEntity()).toDomain() }

    override suspend fun recordSteps(steps: Int) = db.withTransaction {
        val next = Combat.advance((db.heroDao().get() ?: HeroEntity()).toDomain(), steps)
        db.heroDao().save(next.toEntity())
    }
}
