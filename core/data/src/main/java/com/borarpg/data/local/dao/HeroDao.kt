package com.borarpg.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.borarpg.data.local.entity.HeroEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeroDao {
    @Query("SELECT * FROM hero WHERE id = 1") fun observe(): Flow<HeroEntity?>

    @Query("SELECT * FROM hero WHERE id = 1") suspend fun get(): HeroEntity?

    @Upsert suspend fun save(hero: HeroEntity)
}
