package com.borarpg.data

import androidx.room.Room

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
                        .addMigrations(*DatabaseMigrations.all)
                        .build()
                        .also { instance = it }
            }
}
