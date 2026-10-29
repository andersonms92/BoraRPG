package com.borarpg.data.local.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.borarpg.data.local.entity.HeroEntity
import com.borarpg.data.local.entity.ItemEntity
import com.borarpg.data.local.entity.MissionEntity
import com.borarpg.data.local.entity.WorkoutEntity
import com.borarpg.data.local.entity.WorkoutPointEntity
import com.borarpg.data.local.migration.DatabaseMigrations
import com.borarpg.data.mapper.toDomain
import com.borarpg.data.repository.AdventureRepositoryImpl
import com.borarpg.data.repository.MissionRepositoryImpl
import com.borarpg.data.repository.WorkoutRepositoryImpl
import com.borarpg.domain.model.combat.GuardianDefeat
import com.borarpg.domain.model.location.CepLocation
import com.borarpg.domain.model.workout.RoutePoint
import com.borarpg.domain.model.workout.WorkoutStatus
import com.borarpg.domain.rules.mission.QuestGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutPersistenceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun routeAndResultSurviveDatabaseReopen() = runBlocking {
        val name = "workout-persistence-test.db"
        context.deleteDatabase(name)
        val points =
            listOf(RoutePoint(-23.55, -46.63, 1000, 0), RoutePoint(-23.551, -46.63, 2000, 1))
        val original = WorkoutEntity("route", "RUN", 1000, 120000, 200.0, 70.0, "saved")
        var db = Room.databaseBuilder(context, GameDatabase::class.java, name).build()
        try {
            db.workoutDao().save(original)
            db.workoutDao()
                .insertPoints(
                    points.mapIndexed { i, p ->
                        WorkoutPointEntity(
                            original.id,
                            i,
                            p.latitude,
                            p.longitude,
                            p.time,
                            p.segment,
                        )
                    }
                )
            db.close()
            db = Room.databaseBuilder(context, GameDatabase::class.java, name).build()
            val saved = db.workoutDao().observe().first().single()
            assertEquals(original, saved)
            assertEquals(points, db.workoutDao().points(saved.id).map { it.toDomain() })
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun interruptedSessionRecoversPausedWithoutExtraTime() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java).build()
        try {
            db.workoutDao().save(WorkoutEntity("active", "WALK", 1000, 45000))
            db.workoutDao().recover()
            val recovered = db.workoutDao().current()!!
            assertEquals("paused", recovered.status)
            assertEquals(45000L, recovered.elapsedMillis)
            db.workoutDao().delete(recovered.id)
            assertNull(db.workoutDao().current())
        } finally {
            db.close()
        }
    }

    @Test
    fun migrationsPreserveHeroAndCreateWorkoutStorage() = runBlocking {
        val name = "workout-migration-test.db"
        createLegacyDatabase(name, 2) { old ->
            old.execSQL(
                "INSERT INTO hero (id,xp,gold,hp,victories,steps) VALUES (1,42,11,900,0,420)"
            )
        }
        val expectedHero = HeroEntity(xp = 42, gold = 11, hp = 4900, steps = 420)
        var db = openDatabase(name)
        try {
            assertEquals(expectedHero, db.heroDao().get())
            assertEquals(6, db.openHelper.readableDatabase.version)
            db.workoutDao().save(WorkoutEntity("new", "WALK", 1000))
            db.close()
            db = openDatabase(name)
            assertEquals(expectedHero, db.heroDao().get())
            assertEquals("new", db.workoutDao().current()!!.id)
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun rewardCanOnlyBeClaimedOnce() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java).build()
        try {
            val missions = MissionRepositoryImpl(db)
            val quest =
                QuestGenerator.generate(CepLocation("01310100", "Rua", -23.55, -46.63)).first()
            missions.accept(quest)
            db.heroDao().save(HeroEntity(steps = quest.goalSteps))
            missions.claim()
            val hero = db.heroDao().get()!!
            assertEquals(quest.xpReward, hero.xp)
            assertEquals(quest.goldReward, hero.gold)
            assertEquals(1, db.missionDao().inventory().first().single().quantity)
            assertTrue(runCatching { missions.claim() }.isFailure)
            assertEquals(hero, db.heroDao().get())
        } finally {
            db.close()
        }
    }

    @Test
    fun versionFourMigrationPreservesEveryRoutePointAndRewards() = runBlocking {
        val name = "workout-v4-migration-test.db"
        createLegacyDatabase(name, 4) { old ->
            old.execSQL("INSERT INTO hero VALUES (1,42,11,900,0,420)")
            old.execSQL(
                "INSERT INTO workouts VALUES ('legacy','RUN',1000,60000,100.0,72.0,'saved', ?)",
                arrayOf("[[-23.55,-46.63,1000,0],[-23.551,-46.63,5000,1]]"),
            )
        }
        val expectedHero = HeroEntity(xp = 42, gold = 11, hp = 4900, steps = 420)
        var db = openDatabase(name)
        try {
            repeat(2) { iteration ->
                if (iteration > 0) {
                    db.close()
                    db = openDatabase(name)
                }
                val result =
                    WorkoutRepositoryImpl(db, Dispatchers.Default).workouts.first().single()
                assertEquals("legacy", result.id)
                assertEquals(WorkoutStatus.SAVED, result.status)
                assertEquals(60_000L, result.elapsedMillis)
                assertEquals(
                    listOf(
                        RoutePoint(-23.55, -46.63, 1000, 0),
                        RoutePoint(-23.551, -46.63, 5000, 1),
                    ),
                    db.workoutDao().points("legacy").map { it.toDomain() },
                )
                assertEquals(expectedHero, db.heroDao().get())
                assertEquals(6, db.openHelper.readableDatabase.version)
            }
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun versionFiveMigrationPreservesSavedWorkoutMissionsAndInventory() = runBlocking {
        val name = "workout-v5-migration-test.db"
        createLegacyDatabase(name, 5) { old ->
            old.execSQL("INSERT INTO hero VALUES (1,42,11,900,0,420)")
            old.execSQL("INSERT INTO inventory VALUES ('Bota do viajante', 2)")
            old.execSQL(
                "INSERT INTO missions VALUES ('mission','Explorar',-23.55,-46.63,'explore','Explorar a região',2500,180,60,'Bota do viajante','comum',420,'active')"
            )
            old.execSQL(
                "INSERT INTO workouts VALUES ('existing','RUN',1000,60000,100.0,72.0,'saved')"
            )
            old.execSQL("INSERT INTO workout_points VALUES ('existing',0,-23.55,-46.63,1000,0)")
        }
        val expectedHero = HeroEntity(xp = 42, gold = 11, hp = 4900, steps = 420)
        val expectedMission =
            MissionEntity(
                id = "mission",
                title = "Explorar",
                latitude = -23.55,
                longitude = -46.63,
                kind = "explore",
                description = "Explorar a região",
                goalSteps = 2500,
                xpReward = 180,
                goldReward = 60,
                itemReward = "Bota do viajante",
                rarity = "comum",
                baselineSteps = 420,
            )
        var db = openDatabase(name)
        try {
            repeat(2) { iteration ->
                if (iteration > 0) {
                    db.close()
                    db = openDatabase(name)
                }
                assertEquals(expectedHero, db.heroDao().get())
                assertEquals(6, db.openHelper.readableDatabase.version)
                assertEquals(
                    ItemEntity("Bota do viajante", 2),
                    db.missionDao().inventory().first().single(),
                )
                assertEquals(expectedMission, db.missionDao().active())
                val result =
                    WorkoutRepositoryImpl(db, Dispatchers.Default).workouts.first().single()
                assertEquals("existing", result.id)
                assertEquals(WorkoutStatus.SAVED, result.status)
                assertEquals(60_000L, result.elapsedMillis)
                assertEquals(100.0, result.distanceMeters, 0.0)
                assertEquals(72.0, requireNotNull(result.weightKg), 0.0)
                assertEquals(
                    listOf(RoutePoint(-23.55, -46.63, 1000, 0)),
                    db.workoutDao().points(result.id).map { it.toDomain() },
                )
            }
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun versionFiveMigrationPreservesGuardianDamageAndOnlyAwardsTheNextVictory() = runBlocking {
        val name = "guardian-v5-migration-test.db"
        createLegacyDatabase(name, 5) { old ->
            old.execSQL("INSERT INTO hero VALUES (1,699,158,4,1,1996)")
        }
        val migrated = HeroEntity(xp = 699, gold = 158, hp = 4004, victories = 1, steps = 1996)
        var db = openDatabase(name)
        try {
            assertEquals(migrated, db.heroDao().get())
            assertEquals(migrated.toDomain(), AdventureRepositoryImpl(db).hero.first())
            assertEquals(6, db.openHelper.readableDatabase.version)
            db.close()
            db = openDatabase(name)
            val repository = AdventureRepositoryImpl(db)
            assertEquals(migrated, db.heroDao().get())
            assertEquals(migrated.toDomain(), repository.hero.first())
            assertNull(repository.recordSteps(4003))
            assertEquals(
                migrated.copy(xp = 1099, hp = 1, steps = 5999),
                db.heroDao().get(),
            )
            assertEquals(GuardianDefeat(1, 2, 50, 25), repository.recordSteps(1))
            val defeated = HeroEntity(xp = 1150, gold = 183, hp = 5000, victories = 2, steps = 6000)
            assertEquals(defeated, db.heroDao().get())
            db.close()
            db = openDatabase(name)
            assertEquals(defeated, db.heroDao().get())
            assertEquals(defeated.toDomain(), AdventureRepositoryImpl(db).hero.first())
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun versionFiveMigrationWithoutHeroUsesTheNewGuardianHealth() = runBlocking {
        val name = "guardian-empty-v5-migration-test.db"
        createLegacyDatabase(name, 5) {}
        var db = openDatabase(name)
        try {
            assertNull(db.heroDao().get())
            assertEquals(6, db.openHelper.readableDatabase.version)
            assertEquals(HeroEntity(hp = 5000).toDomain(), AdventureRepositoryImpl(db).hero.first())
            assertNull(db.heroDao().get())
            db.close()
            db = openDatabase(name)
            assertNull(db.heroDao().get())
            val repository = AdventureRepositoryImpl(db)
            assertEquals(5000, repository.hero.first().hp)
            assertNull(repository.recordSteps(4999))
            assertEquals(1, db.heroDao().get()!!.hp)
            assertEquals(GuardianDefeat(1, 1, 50, 25), repository.recordSteps(1))
            assertEquals(
                HeroEntity(xp = 550, gold = 25, hp = 5000, victories = 1, steps = 5000),
                db.heroDao().get(),
            )
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }

    private fun openDatabase(name: String): GameDatabase =
        Room.databaseBuilder(context, GameDatabase::class.java, name)
            .addMigrations(*DatabaseMigrations.all)
            .build()

    private fun createLegacyDatabase(name: String, version: Int, seed: (SQLiteDatabase) -> Unit) {
        context.deleteDatabase(name)
        val schema =
            JSONObject(
                    InstrumentationRegistry.getInstrumentation()
                        .context
                        .assets
                        .open("com.borarpg.data.local.database.GameDatabase/$version.json")
                        .bufferedReader()
                        .use { it.readText() }
                )
                .getJSONObject("database")
        val path = context.getDatabasePath(name)
        path.parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(path, null).use { old ->
            val entities = schema.getJSONArray("entities")
            for (index in 0 until entities.length()) {
                val entity = entities.getJSONObject(index)
                old.execSQL(
                    entity
                        .getString("createSql")
                        .replace("\${TABLE_NAME}", entity.getString("tableName"))
                )
            }
            val setup = schema.getJSONArray("setupQueries")
            for (index in 0 until setup.length()) old.execSQL(setup.getString(index))
            seed(old)
            old.version = version
        }
    }
}
