package com.borarpg.feature.activity.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.borarpg.feature.activity.presentation.action.WorkoutAction
import com.borarpg.feature.activity.presentation.formatter.formatActivityTime
import com.borarpg.feature.activity.presentation.formatter.label
import com.borarpg.feature.activity.presentation.state.WorkoutUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun WorkoutDialogs(
    state: WorkoutUiState,
    onAction: (WorkoutAction) -> Unit,
    onHistory: (String) -> Unit,
) {
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
