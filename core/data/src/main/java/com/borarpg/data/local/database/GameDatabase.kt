package com.borarpg.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.borarpg.data.local.dao.HeroDao
import com.borarpg.data.local.dao.MissionDao
import com.borarpg.data.local.dao.WorkoutDao
import com.borarpg.data.local.entity.HeroEntity
import com.borarpg.data.local.entity.ItemEntity
import com.borarpg.data.local.entity.MissionEntity
import com.borarpg.data.local.entity.WorkoutEntity
import com.borarpg.data.local.entity.WorkoutPointEntity

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
