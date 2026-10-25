package com.borarpg.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class ItemEntity(@PrimaryKey val name: String, val quantity: Int)
