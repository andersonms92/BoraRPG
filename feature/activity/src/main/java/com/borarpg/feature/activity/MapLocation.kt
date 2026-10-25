package com.borarpg.feature.activity

import android.location.Location
import com.borarpg.domain.GeoLocation

internal fun GeoLocation.toMapLocation() =
    Location("borarpg").also {
        it.latitude = latitude
        it.longitude = longitude
        it.accuracy = accuracy
    }
