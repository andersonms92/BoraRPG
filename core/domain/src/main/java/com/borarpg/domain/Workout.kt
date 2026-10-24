package com.borarpg.domain

import kotlin.math.*

enum class WorkoutType(val label: String, val met: Double) {
    WALK("Caminhada", 3.8),
    RUN("Corrida", 8.5),
}

data class RoutePoint(val latitude: Double, val longitude: Double, val time: Long, val segment: Int)

fun routeDistance(a: RoutePoint, b: RoutePoint): Double {
    val lat = Math.toRadians(b.latitude - a.latitude)
    val lon = Math.toRadians(b.longitude - a.longitude)
    val h =
        sin(lat / 2).pow(2) +
            cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(lon / 2).pow(2)
    return 6371000 * 2 * asin(sqrt(h.coerceIn(0.0, 1.0)))
}

fun workoutCalories(type: WorkoutType, weight: Double?, elapsed: Long, meters: Double): Double? =
    weight
        ?.takeIf { it.isFinite() && it in 20.0..400.0 }
        ?.let {
            if (meters <= 0) 0.0 else type.met * it * elapsed.coerceAtLeast(0) / 3600000.0
        }

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
