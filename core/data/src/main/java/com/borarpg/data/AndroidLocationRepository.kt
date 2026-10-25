package com.borarpg.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.borarpg.domain.GeoLocation
import com.borarpg.domain.GpsState
import com.borarpg.domain.LocationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class AndroidLocationRepository
@Inject
constructor(@param:ApplicationContext private val context: Context) : LocationRepository {
    override fun updates() = callbackFlow {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val fine =
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        val coarse =
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) {
            trySend(GpsState(message = "Ative a localização para se encontrar no mapa."))
            close()
            return@callbackFlow
        }
        trySend(GpsState())
        val listener =
            object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    trySend(
                        GpsState(
                            GeoLocation(location.latitude, location.longitude, location.accuracy),
                            if (fine) "GPS ativo · precisão de ${location.accuracy.toInt()} m"
                            else "Localização aproximada",
                        )
                    )
                }

                override fun onProviderDisabled(provider: String) {
                    if (
                        manager.getProviders(true).none {
                            it == LocationManager.GPS_PROVIDER ||
                                it == LocationManager.NETWORK_PROVIDER
                        }
                    )
                        trySend(
                            GpsState(message = "Localização desativada. Ative o GPS do aparelho.")
                        )
                }

                override fun onProviderEnabled(provider: String) {
                    trySend(GpsState(message = "Buscando sua localização…"))
                }

                @Deprecated("Legacy callback")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            }
        try {
            val providers =
                listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER).filter {
                    (fine || it != LocationManager.GPS_PROVIDER) && it in manager.allProviders
                }
            if (providers.none { manager.isProviderEnabled(it) })
                trySend(GpsState(message = "Localização desativada. Ative o GPS do aparelho."))
            providers.forEach {
                manager.requestLocationUpdates(it, 3000L, 5f, listener, Looper.getMainLooper())
            }
        } catch (_: SecurityException) {
            trySend(GpsState(message = "Permissão de localização indisponível."))
        } catch (_: IllegalArgumentException) {
            trySend(GpsState(message = "GPS indisponível neste aparelho."))
        }
        awaitClose { manager.removeUpdates(listener) }
    }
}
