package com.borarpg.data

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import com.borarpg.tracking.StepTrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TrackingState(val active: Boolean = false, val error: String? = null)

internal object TrackingRuntime {
    private val mutable = MutableStateFlow(TrackingState())
    val state = mutable.asStateFlow()

    fun update(state: TrackingState) {
        mutable.value = state
    }
}

class TrackingController(context: Context) {
    private val app = context.applicationContext
    val state = TrackingRuntime.state
    val available =
        (app.getSystemService(Context.SENSOR_SERVICE) as SensorManager).getDefaultSensor(
            Sensor.TYPE_STEP_COUNTER
        ) != null

    fun start() {
        if (!available) {
            TrackingRuntime.update(
                TrackingState(error = "Este aparelho não possui contador de passos.")
            )
            return
        }
        try {
            app.startForegroundService(Intent(app, StepTrackingService::class.java))
        } catch (_: RuntimeException) {
            TrackingRuntime.update(
                TrackingState(
                    error = "Não foi possível iniciar. Confira a permissão de atividade física."
                )
            )
        }
    }

    fun stop() {
        app.startService(
            Intent(app, StepTrackingService::class.java).setAction(StepTrackingService.STOP)
        )
    }
}
