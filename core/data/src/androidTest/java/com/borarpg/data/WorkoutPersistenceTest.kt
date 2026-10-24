package com.borarpg.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.borarpg.domain.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
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
        val original =
            WorkoutEntity("route", "RUN", 1000, 120000, 200.0, 70.0, "saved", encodeRoute(points))
        var db = Room.databaseBuilder(context, GameDatabase::class.java, name).build()
        try {
            db.workoutDao().save(original)
            db.close()
            db = Room.databaseBuilder(context, GameDatabase::class.java, name).build()
            val saved = db.workoutDao().observe().first().single()
            assertEquals(original, saved)
            assertEquals(points, saved.points())
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
        context.deleteDatabase(name)
        val schemaText =
            InstrumentationRegistry.getInstrumentation()
                .context
                .assets
                .open("com.borarpg.data.GameDatabase/2.json")
                .bufferedReader()
                .use { it.readText() }
        val schema = JSONObject(schemaText).getJSONObject("database")
        context.getDatabasePath(name).parentFile!!.mkdirs()
        val old = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name), null)
        try {
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                old.execSQL(
                    entity
                        .getString("createSql")
                        .replace("\${TABLE_NAME}", entity.getString("tableName"))
                )
            }
            val setup = schema.getJSONArray("setupQueries")
            for (i in 0 until setup.length()) old.execSQL(setup.getString(i))
            old.execSQL(
                "INSERT INTO hero (id,xp,gold,hp,victories,steps) VALUES (1,42,11,900,0,420)"
            )
            old.version = 2
        } finally {
            old.close()
        }
        val db =
            Room.databaseBuilder(context, GameDatabase::class.java, name)
                .addMigrations(*DatabaseMigrations.all)
                .build()
        try {
            assertEquals(42, db.heroDao().get()!!.xp)
            db.workoutDao().save(WorkoutEntity("new", "WALK", 1000))
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
            val missions = MissionRepository(db)
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
}
