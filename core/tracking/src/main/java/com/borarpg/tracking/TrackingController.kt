package com.borarpg.tracking

import android.content.Context
import android.content.Intent
import com.borarpg.domain.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class TrackingRuntime @Inject constructor() {
    private val mutable = MutableStateFlow(TrackingState())
    val state = mutable.asStateFlow()

    fun update(state: TrackingState) {
        mutable.value = state
    }
}

@Singleton
class TrackingController
@Inject
constructor(
    @param:ApplicationContext private val context: Context,
    private val runtime: TrackingRuntime,
    repository: WorkoutRepository,
) : WorkoutTracker {
    override val state = runtime.state
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val recovery = scope.launch {
        try {
            if (!state.value.active) repository.recoverInterruptedSessions()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            runtime.update(
                state.value.copy(error = "Não foi possível recuperar a atividade anterior.")
            )
        }
    }

    override fun start(type: WorkoutType) = command("start", type)

    override fun finish() = command("finish")

    override fun pause() = command("pause")

    override fun resume() = command("resume")

    override fun cancel() = command("cancel")

    private fun command(action: String, type: WorkoutType = WorkoutType.WALK) {
        scope.launch {
            recovery.join()
            try {
                val intent =
                    Intent(context, StepTrackingService::class.java)
                        .setAction(action)
                        .putExtra("type", type.name)
                if (action == "start" || action == "resume") context.startForegroundService(intent)
                else context.startService(intent)
            } catch (_: RuntimeException) {
                runtime.update(
                    state.value.copy(
                        error =
                            "Não foi possível controlar a atividade. Confira as permissões de localização."
                    )
                )
            }
        }
    }
}
