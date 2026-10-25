package com.borarpg.domain.rules.workout

class WorkoutClock {
    private var accumulated = 0L
    private var start: Long? = null

    fun resume(elapsed: Long, now: Long) {
        accumulated = elapsed.coerceAtLeast(0)
        start = now
    }

    fun elapsed(now: Long): Long = accumulated + (start?.let { (now - it).coerceAtLeast(0) } ?: 0)

    fun pause(now: Long) {
        accumulated = elapsed(now)
        start = null
    }
}
