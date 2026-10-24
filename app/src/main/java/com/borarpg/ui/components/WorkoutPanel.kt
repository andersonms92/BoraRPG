package com.borarpg.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borarpg.data.WorkoutEntity
import com.borarpg.domain.*
import com.borarpg.ui.WorkoutViewModel
import com.borarpg.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun WorkoutPanel(
    vm: WorkoutViewModel,
    current: WorkoutEntity?,
    displayed: WorkoutEntity?,
    workouts: List<WorkoutEntity>,
    onFollow: () -> Unit,
    onHistory: (WorkoutEntity) -> Unit,
) {
    val context = LocalContext.current
    val tracking by vm.tracking.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    var mode by rememberSaveable { mutableStateOf(WorkoutType.WALK) }
    var error by remember { mutableStateOf<String?>(null) }
    var cancel by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf(false) }
    fun start() {
        if (
            !context
                .getSystemService(LocationManager::class.java)
                .isProviderEnabled(LocationManager.GPS_PROVIDER)
        ) {
            error = "Ative o GPS para registrar o percurso."
            return
        }
        if (
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            error = "Autorize a localização precisa para registrar o percurso."
            return
        }
        error = null
        onFollow()
        if (current?.status == "paused") vm.resume() else vm.start(mode)
    }
    val permission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            start()
        }
    fun requestStart() {
        val permissions =
            mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        if (Build.VERSION.SDK_INT >= 29) permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        if (Build.VERSION.SDK_INT >= 33) permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        permission.launch(permissions.toTypedArray())
    }
    val elapsed =
        if (current?.status == "active") maxOf(current.elapsedMillis, tracking.elapsedMillis)
        else displayed?.elapsedMillis ?: 0
    val distance = displayed?.distanceMeters ?: 0.0
    val kcal = displayed?.let { workoutCalories(it.mode, it.weightKg, elapsed, distance) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                formatActivityTime(elapsed),
                style = MaterialTheme.typography.headlineMedium,
                color = Lime,
            )
            TextButton(onClick = { history = true }, enabled = current == null) {
                Text("Histórico")
            }
        }
        Text(
            String.format(
                Locale.getDefault(),
                "%.2f km · %s kcal estimadas",
                distance / 1000,
                kcal?.let { "%.0f".format(it) } ?: "—",
            ),
            style = MaterialTheme.typography.titleMedium,
        )
        if (kcal != null && distance > 0)
            Text(
                String.format(
                    Locale.getDefault(),
                    "%.3f kcal/m · %.0f kcal/km",
                    kcal / distance,
                    kcal / distance * 1000,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        if (current == null)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkoutType.entries.forEach { type ->
                    FilterChip(
                        selected = mode == type,
                        onClick = { mode = type },
                        label = { Text(type.label) },
                    )
                }
            }
        when (current?.status) {
            "active",
            "paused" -> {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (current.status == "paused") requestStart() else vm.pause()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (current.status == "paused") "Retomar" else "Pausar")
                    }
                    OutlinedButton(onClick = vm::finish, modifier = Modifier.weight(1f)) {
                        Text("Finalizar")
                    }
                    TextButton(onClick = { cancel = true }) { Text("Cancelar", color = Coral) }
                }
                Text(
                    if (current.status == "paused") "Atividade pausada"
                    else "${current.mode.label} · gravando percurso GPS",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
            "draft" ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.save(current) }) { Text("Salvar resultado") }
                    OutlinedButton(onClick = { vm.discard(current) }) { Text("Descartar") }
                }
            else ->
                Button(onClick = { requestStart() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Iniciar ${mode.label.lowercase()}")
                }
        }
        if (displayed != null && current == null)
            Text(
                "Último percurso exibido · ${displayed.mode.label}",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        (error ?: tracking.error ?: message)?.let { Text(it, color = Coral) }
    }
    if (cancel)
        AlertDialog(
            onDismissRequest = { cancel = false },
            title = { Text("Cancelar atividade?") },
            text = {
                Text(
                    "O percurso e o resultado serão descartados. Os passos já registrados na jornada permanecem."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        cancel = false
                        if (tracking.active) vm.cancel() else current?.let(vm::discard)
                    }
                ) {
                    Text("Cancelar atividade")
                }
            },
            dismissButton = { TextButton(onClick = { cancel = false }) { Text("Continuar") } },
        )
    if (history)
        AlertDialog(
            onDismissRequest = { history = false },
            title = { Text("Seus percursos") },
            text = {
                Column(
                    Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val saved = workouts.filter { it.status == "saved" }
                    if (saved.isEmpty()) Text("As atividades salvas aparecerão aqui.")
                    saved.forEach { result ->
                        OutlinedButton(
                            onClick = {
                                onHistory(result)
                                history = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                "${SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(result.startedAt))} · ${result.mode.label} · ${"%.2f".format(result.distanceMeters / 1000)} km · ${formatActivityTime(result.elapsedMillis)}"
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { history = false }) { Text("Fechar") } },
        )
}
