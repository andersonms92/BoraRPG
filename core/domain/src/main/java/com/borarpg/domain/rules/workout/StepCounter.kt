package com.borarpg.domain.rules.workout

class StepCounter {
    private var lastReading: Long? = null

    fun record(reading: Long): Int {
        require(reading >= 0)
        val previous = lastReading
        lastReading = reading
        if (previous == null || reading < previous) return 0
        return (reading - previous).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }
}
