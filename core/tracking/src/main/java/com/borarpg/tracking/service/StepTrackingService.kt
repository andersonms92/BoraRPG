package com.borarpg.tracking.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import com.borarpg.core.tracking.R
import com.borarpg.domain.model.location.GeoLocation
import com.borarpg.domain.model.workout.TrackingPhase
import com.borarpg.domain.model.workout.TrackingState
import com.borarpg.domain.model.workout.WorkoutType
import com.borarpg.domain.repository.AdventureRepository
import com.borarpg.domain.repository.PlayerRepository
import com.borarpg.domain.repository.WorkoutRepository
import com.borarpg.domain.rules.workout.WorkoutSessionManager
import com.borarpg.domain.tracking.WorkoutTimeSource
import com.borarpg.tracking.state.TrackingRuntime
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StepTrackingService : Service(), SensorEventListener, LocationListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val commands = Channel<TrackingCommand>(Channel.UNLIMITED)
    @Inject lateinit var repository: WorkoutRepository
    @Inject lateinit var playerRepository: PlayerRepository
    @Inject lateinit var adventureRepository: AdventureRepository
    @Inject lateinit var time: WorkoutTimeSource
    @Inject lateinit var runtime: TrackingRuntime
    private val manager by lazy {
        WorkoutSessionManager(repository, playerRepository, adventureRepository, time) {
            UUID.randomUUID().toString()
        }
    }
    private val sensors by lazy { getSystemService(SENSOR_SERVICE) as SensorManager }
    private val locations by lazy { getSystemService(LOCATION_SERVICE) as LocationManager }
    private var registered = false
    private var timer: Job? = null

    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(
                NotificationChannel(
                    "walking_activity",
                    "Atividade",
                    NotificationManager.IMPORTANCE_LOW,
                )
            )
        scope.launch {
            for (command in commands) {
                try {
                    when (command) {
                        is TrackingCommand.Start -> {
                            ensureLocation()
                            manager.start(command.type)
                        }
                        TrackingCommand.Resume -> {
                            ensureLocation()
                            manager.resume()
                        }
                        TrackingCommand.Pause -> manager.pause()
                        TrackingCommand.Finish -> manager.finish()
                        TrackingCommand.Cancel -> manager.cancel()
                        TrackingCommand.Tick -> manager.tick()
                        TrackingCommand.LocationUnavailable -> manager.locationUnavailable()
                        is TrackingCommand.Position ->
                            manager.location(command.location, command.timestamp)
                        is TrackingCommand.Steps -> manager.steps(command.count)
                    }
                    runtime.update(manager.state.value)
                    if (manager.state.value.phase == TrackingPhase.RUNNING) register()
                    else unregister()
                    if (!manager.state.value.active) shutdown()
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    unregister()
                    try {
                        manager.interrupted()
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        runtime.update(
                            TrackingState(
                                error = "Não foi possível registrar a atividade. Tente novamente."
                            )
                        )
                    }
                    if (manager.state.value.error != null) runtime.update(manager.state.value)
                    shutdown()
                    break
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY
        if (action == "start" || action == "resume") {
            try {
                showNotification()
            } catch (_: RuntimeException) {
                runtime.update(
                    TrackingState(error = "Autorize a localização precisa antes de iniciar.")
                )
                stopSelf()
                return START_NOT_STICKY
            }
        }
        val command =
            when (action) {
                "start" ->
                    TrackingCommand.Start(
                        WorkoutType.entries.firstOrNull { it.name == intent.getStringExtra("type") }
                            ?: WorkoutType.WALK
                    )
                "resume" -> TrackingCommand.Resume
                "pause" -> TrackingCommand.Pause
                "finish" -> TrackingCommand.Finish
                "cancel" -> TrackingCommand.Cancel
                else -> null
            }
        command?.let { commands.trySend(it) }
        return START_NOT_STICKY
    }

    private fun showNotification() {
        val open =
            PendingIntent.getActivity(
                this,
                0,
                checkNotNull(packageManager.getLaunchIntentForPackage(packageName)),
                PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            Notification.Builder(this, "walking_activity")
                .setSmallIcon(R.drawable.ic_tracking)
                .setContentTitle("BoraRPG · percurso em andamento")
                .setContentText("Abra o app para pausar ou finalizar.")
                .setContentIntent(open)
                .setOngoing(true)
                .build()
        if (Build.VERSION.SDK_INT >= 29)
            startForeground(100, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        else startForeground(100, notification)
    }

    private fun ensureLocation() {
        check(
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
        check(locations.isProviderEnabled(LocationManager.GPS_PROVIDER))
    }

    private fun register() {
        if (registered) return
        if (
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
        )
            throw SecurityException("Localização precisa indisponível.")
        locations.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            3000L,
            3f,
            this,
            Looper.getMainLooper(),
        )
        if (
            Build.VERSION.SDK_INT < 29 ||
                checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) ==
                    PackageManager.PERMISSION_GRANTED
        ) {
            sensors.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)?.let {
                sensors.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
        registered = true
        timer = scope.launch {
            while (isActive) {
                delay(1000)
                commands.send(TrackingCommand.Tick)
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        if (registered && location.hasAccuracy())
            commands.trySend(
                TrackingCommand.Position(
                    GeoLocation(location.latitude, location.longitude, location.accuracy),
                    location.elapsedRealtimeNanos / 1_000_000,
                )
            )
    }

    override fun onSensorChanged(event: SensorEvent) {
        val raw = event.values[0]
        if (registered && raw.isFinite() && raw >= 0)
            commands.trySend(TrackingCommand.Steps(raw.toLong()))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onProviderDisabled(provider: String) {
        if (provider == LocationManager.GPS_PROVIDER)
            commands.trySend(TrackingCommand.LocationUnavailable)
    }

    private fun unregister() {
        locations.removeUpdates(this)
        sensors.unregisterListener(this)
        registered = false
        timer?.cancel()
        timer = null
    }

    private fun shutdown() {
        unregister()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        unregister()
        scope.cancel()
        commands.close()
        runtime.update(runtime.state.value.copy(phase = TrackingPhase.IDLE))
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
