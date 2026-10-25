package com.borarpg.domain.rules.workout

import com.borarpg.domain.model.workout.RoutePoint
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteDistanceTest {
    @Test
    fun distanceMatchesOneDegreeAtEquator() {
        assertEquals(
            111194.9,
            routeDistance(RoutePoint(0.0, 0.0, 0, 0), RoutePoint(0.0, 1.0, 1, 0)),
            1.0,
        )
    }

    @Test
    fun identicalPointsHaveNoDistance() {
        val p = RoutePoint(-23.55, -46.63, 0, 0)
        assertEquals(0.0, routeDistance(p, p), .0001)
    }
}
