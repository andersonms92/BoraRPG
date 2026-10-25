package com.borarpg.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.json.JSONArray

internal object DatabaseMigrations {
    val all =
        arrayOf(
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE workouts_new (id TEXT NOT NULL PRIMARY KEY, type TEXT NOT NULL, startedAt INTEGER NOT NULL, elapsedMillis INTEGER NOT NULL, distanceMeters REAL NOT NULL, weightKg REAL, status TEXT NOT NULL)"
                    )
                    db.execSQL(
                        "INSERT INTO workouts_new SELECT id, type, startedAt, elapsedMillis, distanceMeters, weightKg, status FROM workouts"
                    )
                    db.execSQL(
                        "CREATE TABLE workout_points_temp (workoutId TEXT NOT NULL, position INTEGER NOT NULL, latitude REAL NOT NULL, longitude REAL NOT NULL, time INTEGER NOT NULL, segment INTEGER NOT NULL, PRIMARY KEY(workoutId, position))"
                    )
                    db.query("SELECT id, routeJson FROM workouts").use { cursor ->
                        while (cursor.moveToNext()) {
                            val id = cursor.getString(0)
                            val points = JSONArray(cursor.getString(1))
                            for (index in 0 until points.length()) {
                                val point = points.getJSONArray(index)
                                db.execSQL(
                                    "INSERT INTO workout_points_temp VALUES (?, ?, ?, ?, ?, ?)",
                                    arrayOf(
                                        id,
                                        index,
                                        point.getDouble(0),
                                        point.getDouble(1),
                                        point.getLong(2),
                                        point.getInt(3),
                                    ),
                                )
                            }
                        }
                    }
                    db.execSQL("DROP TABLE workouts")
                    db.execSQL("ALTER TABLE workouts_new RENAME TO workouts")
                    db.execSQL(
                        "CREATE TABLE workout_points (workoutId TEXT NOT NULL, position INTEGER NOT NULL, latitude REAL NOT NULL, longitude REAL NOT NULL, time INTEGER NOT NULL, segment INTEGER NOT NULL, PRIMARY KEY(workoutId, position), FOREIGN KEY(workoutId) REFERENCES workouts(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
                    )
                    db.execSQL(
                        "INSERT INTO workout_points SELECT workoutId, position, latitude, longitude, time, segment FROM workout_points_temp"
                    )
                    db.execSQL("DROP TABLE workout_points_temp")
                }
            },
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
