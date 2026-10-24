package com.borarpg.data

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import com.borarpg.tracking.StepTrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

data class TrackingState(
    val active: Boolean = false,
    val error: String? = null,
    val elapsedMillis: Long = 0,
)

internal object TrackingRuntime {
    private val mutable = MutableStateFlow(TrackingState())
    val state = mutable.asStateFlow()

    fun update(state: TrackingState) {
        mutable.value = state
    }

    fun elapsed(milliseconds: Long) {
        mutable.update { it.copy(elapsedMillis = milliseconds.coerceAtLeast(0)) }
    }

    fun stop(milliseconds: Long) {
        mutable.update { it.copy(active = false, elapsedMillis = milliseconds.coerceAtLeast(0)) }
    }
}

class TrackingController(context: Context) {
    private val app = context.applicationContext
    val state =
        combine(TrackingRuntime.state, ActivityTimeRepository(app).lastDuration) { current, last ->
            current.copy(
                elapsedMillis =
                    if (current.active || current.elapsedMillis > 0) current.elapsedMillis else last
            )
        }
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
