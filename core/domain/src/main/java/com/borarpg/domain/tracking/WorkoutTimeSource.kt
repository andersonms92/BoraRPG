package com.borarpg.domain.tracking

interface WorkoutTimeSource {
    fun monotonicMillis(): Long

    fun epochMillis(): Long
}
