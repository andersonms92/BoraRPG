package com.borarpg.feature.activity.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Coral
import com.borarpg.core.designsystem.theme.Lime
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.domain.model.workout.WorkoutStatus
import com.borarpg.domain.model.workout.WorkoutType
import com.borarpg.feature.activity.presentation.action.WorkoutAction
import com.borarpg.feature.activity.presentation.formatter.formatActivityTime
import com.borarpg.feature.activity.presentation.formatter.label
import com.borarpg.feature.activity.presentation.state.WorkoutUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun WorkoutPanel(
    state: WorkoutUiState,
    onAction: (WorkoutAction) -> Unit,
    requestStart: () -> Unit,
    onHistory: (String) -> Unit,
) {
    val current = state.current
    val displayed = state.displayed
    val mode = state.mode
    val elapsed = state.elapsedMillis
    val distance = state.distanceMeters
    val kcal = state.calories
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                formatActivityTime(elapsed),
                style = MaterialTheme.typography.headlineMedium,
                color = Lime,
            )
            TextButton(
                onClick = { onAction(WorkoutAction.ShowHistory) },
                enabled = current == null,
            ) {
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
                        onClick = { onAction(WorkoutAction.SelectMode(type)) },
                        label = { Text(type.label) },
                    )
                }
            }
        when (current?.status) {
            WorkoutStatus.ACTIVE,
            WorkoutStatus.PAUSED -> {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (current.status == WorkoutStatus.PAUSED) requestStart()
                            else onAction(WorkoutAction.Pause)
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (current.status == WorkoutStatus.PAUSED) "Retomar" else "Pausar")
                    }
                    OutlinedButton(
                        onClick = { onAction(WorkoutAction.Finish) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Finalizar")
                    }
                    TextButton(onClick = { onAction(WorkoutAction.ShowCancel) }) {
                        Text("Cancelar", color = Coral)
                    }
                }
                Text(
                    if (current.status == WorkoutStatus.PAUSED) "Atividade pausada"
                    else "${current.mode.label} · gravando percurso GPS",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
            WorkoutStatus.DRAFT ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onAction(WorkoutAction.Save) }, enabled = !state.busy) {
                        Text("Salvar resultado")
                    }
                    OutlinedButton(
                        onClick = { onAction(WorkoutAction.Discard) },
                        enabled = !state.busy,
                    ) {
                        Text("Descartar")
                    }
                }
            else ->
                Button(
                    onClick = requestStart,
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Iniciar ${mode.label.lowercase()}")
                }
        }
        if (displayed != null && current == null)
            Text(
                "Último percurso exibido · ${displayed.mode.label}",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        state.message?.let {
            Text(it, color = Coral)
            TextButton(onClick = { onAction(WorkoutAction.Retry) }) { Text("Tentar novamente") }
        }
    }
    if (state.showCancel)
        AlertDialog(
            onDismissRequest = { onAction(WorkoutAction.HideCancel) },
            title = { Text("Cancelar atividade?") },
            text = {
                Text(
                    "O percurso e o resultado serão descartados. Os passos já registrados na jornada permanecem."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAction(WorkoutAction.HideCancel)
                        onAction(WorkoutAction.Cancel)
                    }
                ) {
                    Text("Cancelar atividade")
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(WorkoutAction.HideCancel) }) { Text("Continuar") }
            },
        )
    if (state.showHistory)
        AlertDialog(
            onDismissRequest = { onAction(WorkoutAction.HideHistory) },
            title = { Text("Seus percursos") },
            text = {
                Column(
                    Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val saved = state.history
                    if (saved.isEmpty()) Text("As atividades salvas aparecerão aqui.")
                    saved.forEach { result ->
                        OutlinedButton(
                            onClick = {
                                onHistory(result.id)
                                onAction(WorkoutAction.HideHistory)
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
            confirmButton = {
                TextButton(onClick = { onAction(WorkoutAction.HideHistory) }) { Text("Fechar") }
            },
        )
}
