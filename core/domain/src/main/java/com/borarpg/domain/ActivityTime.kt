package com.borarpg.domain

fun activityElapsedMillis(startedAt: Long, now: Long): Long = (now - startedAt).coerceAtLeast(0)

fun formatActivityTime(milliseconds: Long): String {
    val seconds = milliseconds.coerceAtLeast(0) / 1000
    return listOf(seconds / 3600, seconds / 60 % 60, seconds % 60).joinToString(":") {
        it.toString().padStart(2, '0')
    }
}
