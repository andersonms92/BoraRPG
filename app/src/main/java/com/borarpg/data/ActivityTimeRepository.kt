package com.borarpg.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.activityTimeStore by preferencesDataStore("activity_time")

class ActivityTimeRepository(context: Context) {
    private val store = context.applicationContext.activityTimeStore
    private val duration = longPreferencesKey("last_duration_millis")
    val lastDuration =
        store.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { (it[duration] ?: 0L).coerceAtLeast(0) }

    suspend fun saveDuration(milliseconds: Long) {
        store.edit { it[duration] = milliseconds.coerceAtLeast(0) }
    }
}
