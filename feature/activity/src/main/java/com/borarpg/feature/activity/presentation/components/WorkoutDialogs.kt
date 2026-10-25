package com.borarpg.feature.activity.presentation.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.feature.activity.presentation.action.WorkoutAction
import com.borarpg.feature.activity.presentation.state.WorkoutUiState

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
        WorkoutHistoryDialog(
            history = state.history,
            profileWeightKg = state.profileWeightKg,
            canSelect = state.current == null,
            onDismiss = { onAction(WorkoutAction.HideHistory) },
            onSelect = { id ->
                onHistory(id)
                onAction(WorkoutAction.HideHistory)
            },
        )
}
