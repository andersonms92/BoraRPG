package com.borarpg.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "hero")
data class HeroEntity(
    @PrimaryKey val id: Int = 1,
    val xp: Int = 0,
    val gold: Int = 0,
    val hp: Int = 1000,
    val victories: Int = 0,
    val steps: Int = 0,
)

@Dao
interface HeroDao {
    @Query("SELECT * FROM hero WHERE id = 1") fun observe(): Flow<HeroEntity?>

    @Query("SELECT * FROM hero WHERE id = 1") suspend fun get(): HeroEntity?

    @Upsert suspend fun save(hero: HeroEntity)
}

@Database(
    entities =
        [
            HeroEntity::class,
            MissionEntity::class,
            ItemEntity::class,
            WorkoutEntity::class,
            WorkoutPointEntity::class,
        ],
    version = 5,
    exportSchema = true,
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun heroDao(): HeroDao

    abstract fun missionDao(): MissionDao

    abstract fun workoutDao(): WorkoutDao
}
