package com.borarpg.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.borarpg.data.local.entity.ItemEntity
import com.borarpg.data.local.entity.MissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface MissionDao {
    @Query("SELECT * FROM missions WHERE status = 'active' LIMIT 1")
    fun observeActive(): Flow<MissionEntity?>

    @Query("SELECT * FROM missions WHERE status = 'active' LIMIT 1")
    suspend fun active(): MissionEntity?

    @Query("SELECT * FROM missions WHERE id = :id") suspend fun find(id: String): MissionEntity?

    @Upsert suspend fun save(mission: MissionEntity)

    @Query("SELECT id FROM missions WHERE status = 'claimed'") fun claimed(): Flow<List<String>>

    @Query("SELECT * FROM inventory ORDER BY name") fun inventory(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM inventory WHERE name = :name") suspend fun item(name: String): ItemEntity?

    @Upsert suspend fun saveItem(item: ItemEntity)
}
