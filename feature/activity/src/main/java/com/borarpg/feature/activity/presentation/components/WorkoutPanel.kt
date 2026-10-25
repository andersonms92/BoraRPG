package com.borarpg.feature.activity.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Elevated
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.core.designsystem.theme.Rose
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
    onEditProfile: () -> Unit,
) {
    val current = state.current
    val displayed = state.displayed
    val mode = state.mode
    val distance = state.distanceMeters
    val kcal = state.calories
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Tempo de atividade",
                    color = Muted,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    formatActivityTime(state.elapsedMillis),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Cream,
                )
            }
            TextButton(
                onClick = { onAction(WorkoutAction.ShowHistory) },
                enabled = current == null,
            ) {
                Icon(Icons.Outlined.History, null, Modifier.size(18.dp))
                Spacer(Modifier.width(5.dp))
                Text("Histórico")
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WorkoutMetric(
                value = String.format(Locale.getDefault(), "%.2f km", distance / 1000),
                label = "Distância",
                icon = Icons.Outlined.Route,
                modifier = Modifier.weight(1f),
            )
            WorkoutMetric(
                value =
                    if (state.calorieWeightKg == null) "Cadastrar peso"
                    else kcal?.let { "%.0f".format(it) } ?: "—",
                label = "kcal estimadas",
                icon = Icons.Outlined.LocalFireDepartment,
                modifier = Modifier.weight(1f),
                onClick = if (state.calorieWeightKg == null) onEditProfile else null,
            )
        }
        if (state.caloriesPerMeter != null && state.caloriesPerKilometer != null)
            Text(
                String.format(
                    Locale.getDefault(),
                    "Média: %.1f kcal/km · %.3f kcal/m",
                    state.caloriesPerKilometer,
                    state.caloriesPerMeter,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        if (state.caloriesUseCurrentWeight) {
            Text(
                "Estimativa com seu peso atual (${"%.1f".format(state.calorieWeightKg)} kg). O peso não foi registrado nesta atividade.",
                color = Muted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (current == null)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkoutType.entries.forEach { type ->
                    val selected = mode == type
                    FilterChip(
                        selected = selected,
                        onClick = { onAction(WorkoutAction.SelectMode(type)) },
                        label = { Text(type.label) },
                        leadingIcon = {
                            Icon(
                                if (type == WorkoutType.RUN)
                                    Icons.AutoMirrored.Outlined.DirectionsRun
                                else Icons.AutoMirrored.Outlined.DirectionsWalk,
                                null,
                                Modifier.size(18.dp),
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            FilterChipDefaults.filterChipColors(
                                containerColor = Panel,
                                labelColor = Muted,
                                iconColor = Muted,
                                selectedContainerColor = Accent.copy(alpha = .14f),
                                selectedLabelColor = Accent,
                                selectedLeadingIconColor = Accent,
                            ),
                        border =
                            FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = Elevated,
                                selectedBorderColor = Accent.copy(alpha = .4f),
                            ),
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
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(
                            if (current.status == WorkoutStatus.PAUSED) Icons.Outlined.PlayArrow
                            else Icons.Outlined.Pause,
                            null,
                            Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (current.status == WorkoutStatus.PAUSED) "Retomar" else "Pausar")
                    }
                    OutlinedButton(
                        onClick = { onAction(WorkoutAction.Finish) },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("Finalizar")
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (current.status == WorkoutStatus.PAUSED) "Atividade pausada"
                        else "${current.mode.label} · gravando percurso GPS",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                    TextButton(onClick = { onAction(WorkoutAction.ShowCancel) }) {
                        Text("Cancelar", color = Accent)
                    }
                }
            }
            WorkoutStatus.DRAFT ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onAction(WorkoutAction.Save) },
                        enabled = !state.busy,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("Salvar resultado")
                    }
                    OutlinedButton(
                        onClick = { onAction(WorkoutAction.Discard) },
                        enabled = !state.busy,
                        modifier = Modifier.heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("Descartar")
                    }
                }
            else ->
                Button(
                    onClick = requestStart,
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    shape = RoundedCornerShape(15.dp),
                ) {
                    Icon(Icons.Outlined.PlayArrow, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Iniciar ${mode.label.lowercase()}")
                }
        }
        if (displayed != null && current == null)
            Text(
                "Atividade salva · ${displayed.mode.label} · ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(displayed.startedAt))}",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        state.message?.let { message ->
            Surface(color = Rose.copy(alpha = .12f), shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(message, color = Cream, style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { onAction(WorkoutAction.Retry) }) {
                        Text("Tentar novamente", color = Accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutMetric(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier =
            if (onClick == null) modifier
            else modifier.clickable(role = Role.Button, onClick = onClick),
        color = Elevated.copy(alpha = .65f),
        shape = RoundedCornerShape(15.dp),
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, null, Modifier.size(14.dp), tint = Accent)
                Text(label, color = Muted, style = MaterialTheme.typography.labelSmall)
            }
            Text(
                value,
                color = if (onClick == null) Cream else Accent,
                style =
                    if (onClick == null) MaterialTheme.typography.titleLarge
                    else MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
