package com.borarpg.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val kind: String,
    val description: String,
    val goalSteps: Int,
    val xpReward: Int,
    val goldReward: Int,
    val itemReward: String,
    val rarity: String,
    val baselineSteps: Int,
    val status: String = "active",
)

@Entity(tableName = "inventory")
data class ItemEntity(@PrimaryKey val name: String, val quantity: Int)

@Dao
interface MissionDao {
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
