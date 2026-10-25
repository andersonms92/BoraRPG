package com.borarpg.feature.activity.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.components.Tag
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Elevated
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.domain.model.location.GeoLocation
import com.borarpg.domain.model.mission.QuestPoint
import com.borarpg.domain.model.workout.RoutePoint
import com.borarpg.domain.rules.workout.routeDistance
import com.borarpg.feature.missions.presentation.components.MissionProgressCard
import com.borarpg.feature.missions.presentation.state.MissionState

@Composable
internal fun ObjectiveDetails(
    selected: QuestPoint,
    mission: MissionState,
    reference: GeoLocation?,
    fromCep: Boolean,
    saved: Boolean,
    onAccept: (QuestPoint) -> Unit,
    onClaim: () -> Unit,
    onCancel: () -> Unit,
    onClose: () -> Unit,
    onToggleSaved: (QuestPoint) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            if (selected.generated) "Objetivo gerado pelo CEP" else "Seu objetivo de exploração",
            color = Accent,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(selected.title, color = Cream, style = MaterialTheme.typography.titleLarge)
        if (selected.description.isNotBlank())
            Text(selected.description, color = Muted, style = MaterialTheme.typography.bodyMedium)
        if (selected.goalSteps > 0) {
            Tag("${selected.rarity} · ${selected.goalSteps} passos")
            Surface(color = Elevated.copy(alpha = .6f), shape = RoundedCornerShape(16.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.StarBorder, null, Modifier.size(24.dp), tint = Accent)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "+${selected.xpReward} XP · ${selected.goldReward} ouro",
                            color = Cream,
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            "Item: ${selected.itemReward}",
                            color = Muted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            val active = mission.active
            if (active != null && active.quest.id == selected.id) {
                MissionProgressCard(active, mission.busy, onClaim, onCancel)
            } else {
                Button(
                    enabled = !mission.busy && active == null,
                    onClick = { onAccept(selected) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Aceitar missão")
                }
                if (active != null)
                    Text(
                        "Conclua ou cancele a missão ativa.",
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall,
                    )
            }
        }
        val distance = reference?.let {
            routeDistance(
                RoutePoint(it.latitude, it.longitude, 0, 0),
                RoutePoint(selected.latitude, selected.longitude, 0, 0),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Place, null, Modifier.size(17.dp), tint = Accent)
            Text(
                if (distance == null) "${selected.kind} · ative o GPS para ver a distância"
                else
                    "${selected.kind} · ${if (distance < 1000) "${distance.toInt()} m" else "%.1f km".format(distance / 1000)} em linha reta${if (fromCep) " a partir do CEP" else " pelo GPS"}",
                color = Muted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            if (selected.generated && selected.goalSteps > 0)
                "Encontro virtual: a meta é de passos. Não é necessário entrar no terreno indicado pelo marcador."
            else if (selected.generated) "Local salvo anteriormente."
            else "Local marcado por você. Visite apenas áreas de acesso público.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onClose) { Text("Fechar") }
            TextButton(
                onClick = {
                    onToggleSaved(selected)
                    onClose()
                }
            ) {
                Icon(Icons.Outlined.BookmarkBorder, null, Modifier.size(17.dp))
                Spacer(Modifier.width(5.dp))
                Text(if (saved) "Remover dos salvos" else "Salvar local")
            }
        }
    }
}
