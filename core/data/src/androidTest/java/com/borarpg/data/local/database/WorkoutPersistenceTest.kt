package com.borarpg.data.local.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.borarpg.data.local.entity.HeroEntity
import com.borarpg.data.local.entity.WorkoutEntity
import com.borarpg.data.local.entity.WorkoutPointEntity
import com.borarpg.data.local.migration.DatabaseMigrations
import com.borarpg.data.mapper.toDomain
import com.borarpg.data.repository.MissionRepositoryImpl
import com.borarpg.data.repository.WorkoutRepositoryImpl
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
        context.deleteDatabase(name)
        val schemaText =
            InstrumentationRegistry.getInstrumentation()
                .context
                .assets
                .open("com.borarpg.data.local.database.GameDatabase/2.json")
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
        context.deleteDatabase(name)
        val schema =
            JSONObject(
                    InstrumentationRegistry.getInstrumentation()
                        .context
                        .assets
                        .open("com.borarpg.data.local.database.GameDatabase/4.json")
                        .bufferedReader()
                        .use { it.readText() }
                )
                .getJSONObject("database")
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
            old.execSQL("INSERT INTO hero VALUES (1,42,11,900,0,420)")
            old.execSQL(
                "INSERT INTO workouts VALUES ('legacy','RUN',1000,60000,100.0,72.0,'saved', ?)",
                arrayOf("[[-23.55,-46.63,1000,0],[-23.551,-46.63,5000,1]]"),
            )
            old.version = 4
        } finally {
            old.close()
        }
        val db =
            Room.databaseBuilder(context, GameDatabase::class.java, name)
                .addMigrations(*DatabaseMigrations.all)
                .build()
        try {
            val result = WorkoutRepositoryImpl(db, Dispatchers.Default).workouts.first().single()
            assertEquals("legacy", result.id)
            assertEquals(WorkoutStatus.SAVED, result.status)
            assertEquals(60_000L, result.elapsedMillis)
            assertEquals(
                listOf(RoutePoint(-23.55, -46.63, 1000, 0), RoutePoint(-23.551, -46.63, 5000, 1)),
                db.workoutDao().points("legacy").map { it.toDomain() },
            )
            assertEquals(42, db.heroDao().get()!!.xp)
            assertEquals(11, db.heroDao().get()!!.gold)
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun existingVersionFiveDatabaseRemainsReadableAfterPackageReorganization() = runBlocking {
        val name = "workout-v5-package-test.db"
        context.deleteDatabase(name)
        val schema =
            JSONObject(
                    InstrumentationRegistry.getInstrumentation()
                        .context
                        .assets
                        .open("com.borarpg.data.local.database.GameDatabase/5.json")
                        .bufferedReader()
                        .use { it.readText() }
                )
                .getJSONObject("database")
        assertEquals("ea976d3679146d8b01d46e65885c1c8c", schema.getString("identityHash"))
        context.getDatabasePath(name).parentFile!!.mkdirs()
        val old = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name), null)
        try {
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
            old.execSQL("INSERT INTO hero VALUES (1,42,11,900,0,420)")
            old.execSQL("INSERT INTO inventory VALUES ('Bota do viajante', 2)")
            old.execSQL(
                "INSERT INTO workouts VALUES ('existing','RUN',1000,60000,100.0,72.0,'saved')"
            )
            old.execSQL("INSERT INTO workout_points VALUES ('existing',0,-23.55,-46.63,1000,0)")
            old.version = 5
        } finally {
            old.close()
        }
        val db = Room.databaseBuilder(context, GameDatabase::class.java, name).build()
        try {
            assertEquals(42, db.heroDao().get()!!.xp)
            assertEquals(2, db.missionDao().inventory().first().single().quantity)
            val result = WorkoutRepositoryImpl(db, Dispatchers.Default).workouts.first().single()
            assertEquals("existing", result.id)
            assertEquals(WorkoutStatus.SAVED, result.status)
            assertEquals(60_000L, result.elapsedMillis)
            assertEquals(
                listOf(RoutePoint(-23.55, -46.63, 1000, 0)),
                db.workoutDao().points(result.id).map { it.toDomain() },
            )
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }
}
