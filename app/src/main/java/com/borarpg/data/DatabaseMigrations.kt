package com.borarpg.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object DatabaseMigrations {
    val all =
        arrayOf(
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("DELETE FROM hero")
                }
            },
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS workouts (id TEXT NOT NULL PRIMARY KEY, type TEXT NOT NULL, startedAt INTEGER NOT NULL, elapsedMillis INTEGER NOT NULL, distanceMeters REAL NOT NULL, weightKg REAL, status TEXT NOT NULL, routeJson TEXT NOT NULL)"
                    )
                }
            },
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS missions (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, latitude REAL NOT NULL, longitude REAL NOT NULL, kind TEXT NOT NULL, description TEXT NOT NULL, goalSteps INTEGER NOT NULL, xpReward INTEGER NOT NULL, goldReward INTEGER NOT NULL, itemReward TEXT NOT NULL, rarity TEXT NOT NULL, baselineSteps INTEGER NOT NULL, status TEXT NOT NULL)"
                    )
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS inventory (name TEXT NOT NULL PRIMARY KEY, quantity INTEGER NOT NULL)"
                    )
                }
            },
        )
}
