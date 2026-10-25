package com.borarpg.data.di

import android.content.Context
import androidx.room.Room
import com.borarpg.data.local.database.GameDatabase
import com.borarpg.data.local.migration.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): GameDatabase =
        Room.databaseBuilder(context, GameDatabase::class.java, "borarpg.db")
            .addMigrations(*DatabaseMigrations.all)
            .build()
}
