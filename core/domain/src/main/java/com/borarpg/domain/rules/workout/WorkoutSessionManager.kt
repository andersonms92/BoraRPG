package com.borarpg.domain.rules.workout

import com.borarpg.domain.model.combat.GuardianDefeat
import com.borarpg.domain.model.location.GeoLocation
import com.borarpg.domain.model.workout.RoutePoint
import com.borarpg.domain.model.workout.TrackingPhase
import com.borarpg.domain.model.workout.TrackingState
import com.borarpg.domain.model.workout.WorkoutSession
import com.borarpg.domain.model.workout.WorkoutStatus
import com.borarpg.domain.model.workout.WorkoutType
import com.borarpg.domain.repository.AdventureRepository
import com.borarpg.domain.repository.PlayerRepository
import com.borarpg.domain.repository.WorkoutRepository
import com.borarpg.domain.tracking.WorkoutTimeSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class WorkoutSessionManager(
    private val repository: WorkoutRepository,
    private val player: PlayerRepository,
    private val adventure: AdventureRepository,
    private val time: WorkoutTimeSource,
    private val newId: () -> String,
) {
    private val mutable = MutableStateFlow(TrackingState())
    val state = mutable.asStateFlow()
    private var session: WorkoutSession? = null
    private val points = mutableListOf<RoutePoint>()
    private var writtenPoints = 0
    private var segment = 0
    private var counter = StepCounter()
    private val clock = WorkoutClock()

    suspend fun start(type: WorkoutType) {
        if (session != null) return
        check(repository.current() == null) { "Conclua a atividade anterior." }
        points.clear()
        writtenPoints = 0
        session =
            WorkoutSession(
                newId(),
                type,
                time.epochMillis(),
                0,
                0.0,
                player.profile.first().weightKg,
                WorkoutStatus.PAUSED,
                emptyList(),
            )
        resume()
    }

    suspend fun resume() {
        if (mutable.value.phase == TrackingPhase.RUNNING) return
        val current = load() ?: return
        if (current.status !in setOf(WorkoutStatus.ACTIVE, WorkoutStatus.PAUSED)) return
        segment = (points.maxOfOrNull { it.segment } ?: -1) + 1
        counter = StepCounter()
        clock.resume(current.elapsedMillis, time.monotonicMillis())
        persist(WorkoutStatus.ACTIVE)
        mutable.value =
            TrackingState(TrackingPhase.RUNNING, current.id, elapsedMillis = current.elapsedMillis)
    }

    suspend fun pause() {
        if (mutable.value.phase != TrackingPhase.RUNNING) return
        clock.pause(time.monotonicMillis())
        persist(WorkoutStatus.PAUSED)
        mutable.value = mutable.value.copy(phase = TrackingPhase.PAUSED, elapsedMillis = elapsed())
    }

    suspend fun finish() {
        val current = load() ?: return
        if (current.status !in setOf(WorkoutStatus.ACTIVE, WorkoutStatus.PAUSED)) return
        clock.pause(time.monotonicMillis())
        persist(WorkoutStatus.DRAFT)
        mutable.value = TrackingState(elapsedMillis = elapsed())
        session = null
    }

    suspend fun cancel() {
        val current = session ?: repository.current()
        if (current != null && current.status != WorkoutStatus.SAVED) repository.delete(current.id)
        clock.pause(time.monotonicMillis())
        session = null
        mutable.value = TrackingState()
    }

    suspend fun tick() {
        if (mutable.value.phase != TrackingPhase.RUNNING) return
        persist(WorkoutStatus.ACTIVE)
        mutable.value = mutable.value.copy(elapsedMillis = elapsed())
    }

    suspend fun location(location: GeoLocation, timestamp: Long) {
        if (mutable.value.phase != TrackingPhase.RUNNING) return
        if (
            !location.latitude.isFinite() ||
                location.latitude !in -90.0..90.0 ||
                !location.longitude.isFinite() ||
                location.longitude !in -180.0..180.0 ||
                !location.accuracy.isFinite() ||
                location.accuracy !in 0f..35f ||
                time.monotonicMillis() - timestamp !in 0..15_000
        )
            return
        val current = checkNotNull(session)
        val next = RoutePoint(location.latitude, location.longitude, timestamp, segment)
        val last = points.lastOrNull()?.takeIf { it.segment == segment }
        var distance = 0.0
        if (last != null) {
            val seconds = (timestamp - last.time) / 1000.0
            if (seconds <= 0) return
            distance = routeDistance(last, next)
            if (distance < maxOf(3.0, location.accuracy * .5)) return
            if (seconds > 60) {
                segment++
                distance = 0.0
            } else if (distance / seconds > if (current.mode == WorkoutType.WALK) 4.5 else 12.5)
                return
        }
        points.add(next.copy(segment = segment))
        session = current.copy(distanceMeters = current.distanceMeters + distance)
        persist(WorkoutStatus.ACTIVE)
        mutable.value =
            mutable.value.copy(location = location, elapsedMillis = elapsed(), error = null)
    }

    fun locationUnavailable() {
        if (mutable.value.phase == TrackingPhase.RUNNING)
            mutable.value =
                mutable.value.copy(
                    error = "GPS desligado. Ative-o para continuar registrando o percurso."
                )
    }

    suspend fun steps(raw: Long): GuardianDefeat? {
        if (mutable.value.phase != TrackingPhase.RUNNING || raw < 0) return null
        val delta = counter.record(raw)
        return if (delta > 0) adventure.recordSteps(delta) else null
    }

    suspend fun interrupted() {
        clock.pause(time.monotonicMillis())
        repository.recoverInterruptedSessions()
        mutable.value =
            TrackingState(
                error = "A atividade foi interrompida. O último percurso gravado pode ser retomado."
            )
    }

    private suspend fun load(): WorkoutSession? {
        if (session == null) {
            session =
                repository
                    .current()
                    ?.also {
                        points.clear()
                        points.addAll(it.route)
                        writtenPoints = points.size
                        clock.resume(it.elapsedMillis, time.monotonicMillis())
                        clock.pause(time.monotonicMillis())
                    }
                    ?.copy(route = emptyList())
        }
        return session
    }

    private fun elapsed() = clock.elapsed(time.monotonicMillis())

    private suspend fun persist(status: WorkoutStatus) {
        val current = checkNotNull(session).copy(status = status, elapsedMillis = elapsed())
        repository.checkpoint(
            current,
            points.subList(writtenPoints, points.size).toList(),
            writtenPoints,
        )
        writtenPoints = points.size
        session = current
    }
}
