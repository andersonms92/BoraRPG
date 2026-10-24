package com.borarpg.tracking

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import com.borarpg.domain.WorkoutType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TrackingState(
    val active: Boolean = false,
    val error: String? = null,
    val elapsedMillis: Long = 0,
    val paused: Boolean = false,
    val location: Location? = null,
)

internal object TrackingRuntime {
    private val mutable = MutableStateFlow(TrackingState())
    val state = mutable.asStateFlow()

    fun update(state: TrackingState) {
        mutable.value = state
    }

    fun elapsed(milliseconds: Long) {
        mutable.update { it.copy(elapsedMillis = milliseconds) }
    }

    fun stop(milliseconds: Long) {
        mutable.update { it.copy(active = false, paused = false, elapsedMillis = milliseconds) }
    }
}

@Singleton
class TrackingController @Inject constructor(@ApplicationContext context: Context) {
    private val app = context.applicationContext
    val state = TrackingRuntime.state
    val available =
        (app.getSystemService(Context.SENSOR_SERVICE) as SensorManager).getDefaultSensor(
            Sensor.TYPE_STEP_COUNTER
        ) != null

    fun start(type: WorkoutType = WorkoutType.WALK) = command("start", type)

    fun stop() = command(StepTrackingService.STOP)

    fun pause() = command("pause")

    fun resume() = command("resume")

    fun cancel() = command("cancel")

    private fun command(action: String, type: WorkoutType = WorkoutType.WALK) {
        try {
            val intent =
                Intent(app, StepTrackingService::class.java)
                    .setAction(action)
                    .putExtra("type", type.name)
            if (action == "start" || action == "resume") app.startForegroundService(intent)
            else app.startService(intent)
        } catch (_: RuntimeException) {
            TrackingRuntime.update(
                TrackingRuntime.state.value.copy(
                    error =
                        "Não foi possível controlar a atividade. Confira as permissões de localização."
                )
            )
        }
    }
}
