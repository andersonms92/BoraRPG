package com.borarpg.tracking

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.*
import android.location.*
import android.os.*
import com.borarpg.core.tracking.R
import com.borarpg.data.*
import com.borarpg.domain.*
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class StepTrackingService : Service(), SensorEventListener, LocationListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val commands = Channel<suspend () -> Unit>(Channel.UNLIMITED)
    @Inject lateinit var dao: WorkoutDao
    @Inject lateinit var playerRepository: PlayerRepository
    @Inject lateinit var adventureRepository: AdventureRepository
    private val sensors by lazy { getSystemService(SENSOR_SERVICE) as SensorManager }
    private val locations by lazy { getSystemService(LOCATION_SERVICE) as LocationManager }
    private var workout: WorkoutEntity? = null
    private val points = mutableListOf<RoutePoint>()
    private var counter = StepCounter()
    private var segment = 0
    private val clock = WorkoutClock()
    private var running = false
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
                    command()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    clock.pause(SystemClock.elapsedRealtime())
                    running = false
                    unregister()
                    runCatching { dao.recover() }
                    TrackingRuntime.update(
                        TrackingRuntime.state.value.copy(
                            active = false,
                            error =
                                "A atividade foi interrompida. O último percurso gravado pode ser retomado.",
                        )
                    )
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: "start"
        if (action == "start" || action == "resume") {
            try {
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
            } catch (_: RuntimeException) {
                TrackingRuntime.update(
                    TrackingState(error = "Autorize a localização precisa antes de iniciar.")
                )
                stopSelf()
                return START_NOT_STICKY
            }
        }
        commands.trySend {
            when (action) {
                "start" ->
                    if (workout == null) {
                        check(dao.current() == null) { "Conclua a atividade anterior." }
                        val weight =
                            playerRepository.profile
                                .first()
                                .weight
                                .replace(',', '.')
                                .toDoubleOrNull()
                        workout =
                            WorkoutEntity(
                                UUID.randomUUID().toString(),
                                intent?.getStringExtra("type") ?: "WALK",
                                System.currentTimeMillis(),
                                weightKg = weight,
                            )
                        resumeTracking()
                    }
                "resume" -> {
                    if (workout == null)
                        workout =
                            dao.current()?.also {
                                points.addAll(it.points())
                            }
                    if (workout?.status == "paused" || workout?.status == "active") resumeTracking()
                }
                "pause" ->
                    if (running) {
                        clock.pause(SystemClock.elapsedRealtime())
                        checkpoint("paused")
                        running = false
                        unregister()
                        TrackingRuntime.update(
                            TrackingRuntime.state.value.copy(active = true, paused = true)
                        )
                    }
                STOP -> {
                    if (workout == null)
                        workout =
                            dao.current()?.also {
                                points.addAll(it.points())
                                clock.resume(it.elapsedMillis, SystemClock.elapsedRealtime())
                                clock.pause(SystemClock.elapsedRealtime())
                            }
                    if (workout != null) {
                        checkpoint("draft")
                        shutdown()
                    }
                }
                "cancel" -> {
                    (workout ?: dao.current())?.let { dao.delete(it.id) }
                    shutdown()
                }
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun resumeTracking() {
        if (running) return
        check(
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
        check(locations.isProviderEnabled(LocationManager.GPS_PROVIDER))
        segment = (points.maxOfOrNull { it.segment } ?: -1) + 1
        counter = StepCounter()
        clock.resume(workout!!.elapsedMillis, SystemClock.elapsedRealtime())
        running = true
        workout = workout!!.copy(status = "active")
        dao.save(workout!!)
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
        TrackingRuntime.update(
            TrackingState(active = true, elapsedMillis = workout!!.elapsedMillis)
        )
        timer?.cancel()
        timer = scope.launch {
            while (isActive) {
                delay(1000)
                if (running) {
                    TrackingRuntime.elapsed(elapsed())
                    commands.send { if (running) checkpoint("active") }
                }
            }
        }
    }

    private fun elapsed() = clock.elapsed(SystemClock.elapsedRealtime())

    private suspend fun checkpoint(status: String) {
        workout =
            workout?.copy(
                elapsedMillis = elapsed(),
                status = status,
                routeJson = encodeRoute(points),
            )
        workout?.let { dao.save(it) }
    }

    override fun onLocationChanged(location: Location) {
        if (!running) return
        val copy = Location(location)
        commands.trySend {
            if (
                !running ||
                    !copy.hasAccuracy() ||
                    copy.accuracy > 35 ||
                    SystemClock.elapsedRealtimeNanos() - copy.elapsedRealtimeNanos > 15000000000L
            )
                return@trySend
            val next =
                RoutePoint(
                    copy.latitude,
                    copy.longitude,
                    copy.elapsedRealtimeNanos / 1000000,
                    segment,
                )
            val last = points.lastOrNull()?.takeIf { it.segment == segment }
            var distance = 0.0
            if (last != null) {
                val seconds = (next.time - last.time) / 1000.0
                if (seconds <= 0) return@trySend
                distance = routeDistance(last, next)
                if (distance < maxOf(3.0, copy.accuracy * .5)) return@trySend
                if (seconds > 60) {
                    segment++
                    distance = 0.0
                } else if (
                    distance / seconds > if (workout!!.mode == WorkoutType.WALK) 4.5 else 12.5
                )
                    return@trySend
            }
            points.add(next.copy(segment = segment))
            workout = workout!!.copy(distanceMeters = workout!!.distanceMeters + distance)
            TrackingRuntime.update(TrackingRuntime.state.value.copy(location = copy))
            checkpoint("active")
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val raw = event.values[0]
        if (running && raw.isFinite() && raw >= 0)
            commands.trySend {
                if (running) {
                    val delta = counter.record(raw.toLong())
                    if (delta > 0) adventureRepository.recordSteps(delta)
                }
            }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onProviderDisabled(provider: String) {
        TrackingRuntime.update(
            TrackingRuntime.state.value.copy(
                error = "GPS desligado. Ative-o para continuar registrando o percurso."
            )
        )
    }

    private fun unregister() {
        locations.removeUpdates(this)
        sensors.unregisterListener(this)
        timer?.cancel()
    }

    private fun shutdown() {
        clock.pause(SystemClock.elapsedRealtime())
        running = false
        unregister()
        TrackingRuntime.stop(workout?.elapsedMillis ?: 0)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        unregister()
        scope.cancel()
        commands.close()
        TrackingRuntime.stop(workout?.elapsedMillis ?: 0)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val STOP = "com.borarpg.STOP_TRACKING"
    }
}
