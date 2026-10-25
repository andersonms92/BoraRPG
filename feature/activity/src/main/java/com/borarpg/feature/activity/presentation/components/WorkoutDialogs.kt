package com.borarpg.feature.activity.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
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
            icon = { Icon(Icons.Outlined.DeleteOutline, null, tint = Accent) },
            title = { Text("Cancelar atividade?") },
            containerColor = Panel,
            titleContentColor = Cream,
            textContentColor = Muted,
            shape = RoundedCornerShape(28.dp),
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
            icon = { Icon(Icons.Outlined.Route, null, tint = Accent) },
            title = { Text("Seus percursos") },
            containerColor = Panel,
            titleContentColor = Cream,
            textContentColor = Muted,
            shape = RoundedCornerShape(28.dp),
            text = {
                Column(
                    Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (state.history.isEmpty()) Text("As atividades salvas aparecerão aqui.")
                    state.history.forEach { result ->
                        OutlinedButton(
                            onClick = {
                                onHistory(result.id)
                                onAction(WorkoutAction.HideHistory)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(14.dp),
                        ) {
                            Column(
                                Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        result.mode.label,
                                        color = Cream,
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    Text(
                                        SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                                            .format(Date(result.startedAt)),
                                        color = Muted,
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                                Text(
                                    "${"%.2f".format(result.distanceMeters / 1000)} km · ${formatActivityTime(result.elapsedMillis)}",
                                    color = Accent,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onAction(WorkoutAction.HideHistory) }) { Text("Fechar") }
            },
        )
}
