package com.borarpg.feature.activity.presentation.map

import android.location.Location
import com.borarpg.domain.model.location.GeoLocation

internal fun GeoLocation.toMapLocation() =
    Location("borarpg").also {
        it.latitude = latitude
        it.longitude = longitude
        it.accuracy = accuracy
    }
