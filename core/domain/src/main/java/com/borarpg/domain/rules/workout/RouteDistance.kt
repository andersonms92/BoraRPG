package com.borarpg.domain.rules.workout

import com.borarpg.domain.model.workout.RoutePoint
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun routeDistance(a: RoutePoint, b: RoutePoint): Double {
    val lat = Math.toRadians(b.latitude - a.latitude)
    val lon = Math.toRadians(b.longitude - a.longitude)
    val h =
        sin(lat / 2).pow(2) +
            cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(lon / 2).pow(2)
    return 6371000 * 2 * asin(sqrt(h.coerceIn(0.0, 1.0)))
}
