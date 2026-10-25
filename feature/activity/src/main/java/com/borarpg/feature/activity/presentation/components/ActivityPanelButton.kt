package com.borarpg.feature.activity.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Ink
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.core.designsystem.theme.Rose
import com.borarpg.domain.model.workout.WorkoutStatus
import com.borarpg.domain.model.workout.WorkoutType

@Composable
internal fun ActivityPanelButton(
    expanded: Boolean,
    status: WorkoutStatus?,
    mode: WorkoutType,
    hasError: Boolean,
    onClick: () -> Unit,
) {
    val sessionDescription =
        when {
            hasError -> "Atividade precisa de atenção"
            status == WorkoutStatus.ACTIVE -> "Atividade em andamento"
            status == WorkoutStatus.PAUSED -> "Atividade pausada"
            status == WorkoutStatus.DRAFT -> "Resultado aguardando salvamento"
            else -> "Pronto para iniciar"
        }
    BadgedBox(
        badge = {
            if (
                !expanded &&
                    (hasError ||
                        status in
                            setOf(WorkoutStatus.ACTIVE, WorkoutStatus.PAUSED, WorkoutStatus.DRAFT))
            ) {
                Badge(containerColor = if (hasError) Accent else Rose, contentColor = Cream) {
                    Icon(
                        imageVector =
                            when {
                                hasError -> Icons.Outlined.WarningAmber
                                status == WorkoutStatus.ACTIVE -> Icons.Outlined.PlayArrow
                                status == WorkoutStatus.PAUSED -> Icons.Outlined.Pause
                                else -> Icons.Outlined.Check
                            },
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    ) {
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(52.dp).semantics { stateDescription = sessionDescription },
            shape = RoundedCornerShape(18.dp),
            colors =
                IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (expanded) Accent else Panel.copy(alpha = .97f),
                    contentColor = if (expanded) Ink else Accent,
                ),
        ) {
            Icon(
                imageVector =
                    when {
                        expanded -> Icons.Outlined.ExpandMore
                        mode == WorkoutType.RUN -> Icons.AutoMirrored.Outlined.DirectionsRun
                        else -> Icons.AutoMirrored.Outlined.DirectionsWalk
                    },
                contentDescription =
                    if (expanded) "Minimizar painel de atividade"
                    else "Expandir painel de atividade",
            )
        }
    }
}
