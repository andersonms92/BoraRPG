package com.borarpg.feature.activity.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.domain.model.workout.WorkoutSession
import com.borarpg.domain.rules.workout.resolveWorkoutWeight
import com.borarpg.domain.rules.workout.workoutCalories
import com.borarpg.feature.activity.presentation.formatter.formatActivityTime
import com.borarpg.feature.activity.presentation.formatter.label
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun WorkoutHistoryDialog(
    history: List<WorkoutSession>,
    profileWeightKg: Double?,
    canSelect: Boolean,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val maxHeight = (LocalConfiguration.current.screenHeightDp * .85f).dp
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier =
                Modifier.padding(horizontal = 20.dp)
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .heightIn(max = maxHeight),
            shape = RoundedCornerShape(28.dp),
            color = Panel,
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.History, null, tint = Accent)
                    Text(
                        "Histórico de atividades",
                        color = Cream,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                Text(
                    if (canSelect)
                        "Escolha uma atividade salva para consultar o resultado e o percurso no mapa."
                    else
                        "Finalize a atividade atual para abrir percursos anteriores. Você pode consultar os resultados abaixo.",
                    color = Muted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (history.isEmpty()) {
                    Text(
                        "Nenhuma atividade salva ainda. Ao finalizar um exercício, toque em Salvar resultado.",
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Cream,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(history, key = { it.id }) { result ->
                            WorkoutHistoryItem(
                                result,
                                profileWeightKg,
                                canSelect,
                                onClick = { onSelect(result.id) },
                            )
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Fechar")
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryItem(
    result: WorkoutSession,
    profileWeightKg: Double?,
    canSelect: Boolean,
    onClick: () -> Unit,
) {
    val calories =
        workoutCalories(
            result.mode,
            resolveWorkoutWeight(result.weightKg, profileWeightKg),
            result.elapsedMillis,
            result.distanceMeters,
        )
    Surface(
        onClick = onClick,
        enabled = canSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Cream.copy(alpha = .035f),
        border = BorderStroke(1.dp, Cream.copy(alpha = .08f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(result.mode.label, color = Cream, style = MaterialTheme.typography.titleMedium)
            Text(
                SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
                    .format(Date(result.startedAt)),
                color = Muted,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "${"%.2f".format(result.distanceMeters / 1000)} km · ${formatActivityTime(result.elapsedMillis)}",
                color = Cream,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (calories != null) {
                val average =
                    if (result.distanceMeters > 0)
                        " · ${(calories / (result.distanceMeters / 1000)).roundToInt()} kcal/km"
                    else ""
                Text(
                    "${calories.roundToInt()} kcal estimadas$average",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall,
                )
                if (resolveWorkoutWeight(result.weightKg) == null) {
                    Text(
                        "Estimativa com seu peso atual",
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            } else {
                Text(
                    "Informe seu peso em Você para estimar kcal.",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (canSelect) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Route, null, modifier = Modifier.size(18.dp), tint = Accent)
                    Text(
                        "Ver percurso",
                        color = Accent,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
