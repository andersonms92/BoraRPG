package com.borarpg.data

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object DatabaseProvider {
    @Volatile private var instance: GameDatabase? = null

    fun get(context: android.content.Context): GameDatabase =
        instance
            ?: synchronized(this) {
                instance
                    ?: Room.databaseBuilder(
                            context.applicationContext,
                            GameDatabase::class.java,
                            "borarpg.db",
                        )
                        .addMigrations(
                            object : Migration(1, 2) {
                                override fun migrate(db: SupportSQLiteDatabase) {
                                    db.execSQL("DELETE FROM hero")
                                }
                            }
                        )
                        .build()
                        .also { instance = it }
            }
}
