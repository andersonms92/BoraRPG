package com.borarpg.tracking

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.*
import android.os.Build
import android.os.IBinder
import com.borarpg.MainActivity
import com.borarpg.R
import com.borarpg.data.*
import com.borarpg.domain.StepCounter
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

class StepTrackingService : Service(), SensorEventListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val readings = Channel<Long>(Channel.UNLIMITED)
    private lateinit var sensors: SensorManager
    private var tracking = false
    private var finishing = false
    private lateinit var worker: Job

    override fun onCreate() {
        super.onCreate()
        sensors = getSystemService(SENSOR_SERVICE) as SensorManager
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(
                NotificationChannel(
                    CHANNEL,
                    "Atividade em andamento",
                    NotificationManager.IMPORTANCE_LOW,
                )
            )
        worker = scope.launch {
            val counter = StepCounter()
            val repository = AdventureRepository(DatabaseProvider.get(applicationContext))
            try {
                for (reading in readings) {
                    val steps = counter.record(reading)
                    if (steps > 0) repository.recordSteps(steps)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    TrackingRuntime.update(
                        TrackingState(
                            error = "A atividade parou porque não foi possível salvar o progresso."
                        )
                    )
                    finishTracking()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == STOP) {
            finishTracking()
            return START_NOT_STICKY
        }
        if (tracking || finishing) return START_NOT_STICKY
        if (
            Build.VERSION.SDK_INT >= 29 &&
                checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) !=
                    PackageManager.PERMISSION_GRANTED
        ) {
            TrackingRuntime.update(
                TrackingState(error = "Autorize a atividade física para contar seus passos.")
            )
            stopSelf()
            return START_NOT_STICKY
        }
        val sensor = sensors.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (sensor == null) {
            TrackingRuntime.update(
                TrackingState(error = "Contador de passos indisponível neste aparelho.")
            )
            stopSelf()
            return START_NOT_STICKY
        }
        try {
            val notification = notification()
            if (Build.VERSION.SDK_INT >= 34)
                startForeground(
                    NOTIFICATION,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH,
                )
            else startForeground(NOTIFICATION, notification)
            if (!sensors.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)) {
                TrackingRuntime.update(
                    TrackingState(error = "Não foi possível acessar o contador de passos.")
                )
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            tracking = true
            TrackingRuntime.update(TrackingState(active = true))
        } catch (_: RuntimeException) {
            TrackingRuntime.update(
                TrackingState(
                    error = "Não foi possível iniciar a atividade. Confira as permissões."
                )
            )
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (tracking && !finishing && event.values[0].isFinite() && event.values[0] >= 0)
            readings.trySend(event.values[0].toLong())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onBind(intent: Intent?): IBinder? = null

    private fun finishTracking() {
        if (finishing) return
        finishing = true
        sensors.unregisterListener(this)
        readings.close()
        scope.launch {
            worker.join()
            withContext(Dispatchers.Main) {
                if (TrackingRuntime.state.value.active) TrackingRuntime.update(TrackingState())
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        sensors.unregisterListener(this)
        readings.close()
        scope.cancel()
        if (TrackingRuntime.state.value.active) TrackingRuntime.update(TrackingState())
        super.onDestroy()
    }

    private fun notification(): Notification {
        val open =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        val stop =
            PendingIntent.getService(
                this,
                1,
                Intent(this, StepTrackingService::class.java).setAction(STOP),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_bora)
            .setContentTitle("BoraRPG · atividade em andamento")
            .setContentText("Seus passos estão avançando a jornada.")
            .setContentIntent(open)
            .setOngoing(true)
            .addAction(Notification.Action.Builder(null, "Encerrar", stop).build())
            .build()
    }

    companion object {
        const val STOP = "com.borarpg.STOP_TRACKING"
        private const val CHANNEL = "walking_activity"
        private const val NOTIFICATION = 100
    }
}
