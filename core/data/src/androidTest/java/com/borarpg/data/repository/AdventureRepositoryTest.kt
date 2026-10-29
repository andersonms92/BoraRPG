package com.borarpg.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.borarpg.data.local.database.GameDatabase
import com.borarpg.data.local.entity.HeroEntity
import com.borarpg.domain.model.combat.GuardianDefeat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdventureRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private suspend fun withStore(test: suspend (GameDatabase, AdventureRepositoryImpl) -> Unit) {
        val db = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java).build()
        try {
            test(db, AdventureRepositoryImpl(db))
        } finally {
            db.close()
        }
    }

    @Test
    fun victoryEventIsReturnedAfterStepsAndRewardsArePersisted() = runBlocking {
        withStore { db, repository ->
            assertNull(repository.recordSteps(999))
            assertEquals(1, db.heroDao().get()!!.hp)
            assertEquals(GuardianDefeat(1, 1, 50, 25), repository.recordSteps(1))
            val hero = db.heroDao().get()!!
            assertEquals(1000, hero.steps)
            assertEquals(1000, hero.hp)
            assertEquals(1, hero.victories)
            assertEquals(150, hero.xp)
            assertEquals(25, hero.gold)
        }
    }

    @Test
    fun batchedVictoriesReturnOneEventContainingOnlyVictoryBonuses() = runBlocking {
        withStore { db, repository ->
            assertEquals(GuardianDefeat(2, 2, 100, 50), repository.recordSteps(2501))
            val hero = db.heroDao().get()!!
            assertEquals(350, hero.xp)
            assertEquals(50, hero.gold)
            assertEquals(499, hero.hp)
        }
    }

    @Test
    fun existingVictoriesNeverReplayWhenOpeningOrContinuingProgress() = runBlocking {
        withStore { db, repository ->
            db.heroDao().save(HeroEntity(xp = 699, gold = 158, hp = 4, victories = 1, steps = 1996))
            assertEquals(1, repository.hero.first().victories)
            val reopened = AdventureRepositoryImpl(db)
            assertEquals(1, reopened.hero.first().victories)
            assertNull(reopened.recordSteps(3))
            assertEquals(GuardianDefeat(1, 2, 50, 25), reopened.recordSteps(1))
            assertEquals(2, db.heroDao().get()!!.victories)
        }
    }

    @Test
    fun concurrentStepBatchesEachReturnOnlyTheirCommittedVictory() = runBlocking {
        withStore { db, repository ->
            val events = coroutineScope {
                listOf(
                        async(Dispatchers.Default) { repository.recordSteps(1000) },
                        async(Dispatchers.Default) { repository.recordSteps(1000) },
                    )
                    .awaitAll()
                    .filterNotNull()
            }
            assertEquals(listOf(1, 2), events.map { it.totalVictories }.sorted())
            assertTrue(events.all { it.victories == 1 && it.bonusXp == 50 && it.gold == 25 })
            assertEquals(2, db.heroDao().get()!!.victories)
            assertEquals(2000, db.heroDao().get()!!.steps)
        }
    }

    @Test
    fun failedTransactionCannotReturnAVictoryOrPersistItsRewards() = runBlocking {
        withStore { db, repository ->
            val original = HeroEntity(hp = 1, steps = 999)
            db.heroDao().save(original)
            db.openHelper.writableDatabase.execSQL(
                "CREATE TRIGGER reject_hero_update BEFORE UPDATE ON hero BEGIN SELECT RAISE(ABORT, 'failure'); END"
            )
            var event: GuardianDefeat? = null
            assertTrue(runCatching { event = repository.recordSteps(1) }.isFailure)
            assertNull(event)
            assertEquals(original, db.heroDao().get())
        }
    }
}
