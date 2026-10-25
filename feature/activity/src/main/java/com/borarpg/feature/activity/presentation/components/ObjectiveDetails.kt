package com.borarpg.feature.activity.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.borarpg.core.designsystem.components.Eyebrow
import com.borarpg.core.designsystem.components.Tag
import com.borarpg.core.designsystem.theme.Coral
import com.borarpg.core.designsystem.theme.Lime
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
    Eyebrow(
        if (selected.generated) "Objetivo gerado pelo CEP" else "Seu objetivo de exploração",
        Coral,
    )
    Text(selected.title, style = MaterialTheme.typography.titleLarge)
    if (selected.description.isNotBlank()) Text(selected.description, color = Lime)
    if (selected.goalSteps > 0) {
        Tag("${selected.rarity} · ${selected.goalSteps} passos")
        Text(
            "+${selected.xpReward} XP · ${selected.goldReward} ouro",
            color = Lime,
        )
        Text("Item: ${selected.itemReward}", color = Coral)
        if (mission.active?.quest?.id == selected.id) {
            MissionProgressCard(
                mission.active!!,
                mission.busy,
                onClaim,
                onCancel,
            )
        } else {
            Button(
                enabled = !mission.busy && mission.active == null,
                onClick = { onAccept(selected) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Aceitar missão")
            }
            if (mission.active != null)
                Text(
                    "Conclua ou cancele a missão ativa.",
                    color = Muted,
                )
        }
    }
    val distance = reference?.let {
        routeDistance(
            RoutePoint(it.latitude, it.longitude, 0, 0),
            RoutePoint(selected.latitude, selected.longitude, 0, 0),
        )
    }
    Text(
        if (distance == null) "${selected.kind} · ative o GPS para ver a distância"
        else
            "${selected.kind} · ${if (distance < 1000) "${distance.toInt()} m" else "%.1f km".format(distance / 1000)} em linha reta${if (fromCep) " a partir do CEP" else " pelo GPS"}",
        color = Muted,
    )
    Text(
        if (selected.generated && selected.goalSteps > 0)
            "Encontro virtual: a meta é de passos. Não é necessário entrar no terreno indicado pelo marcador."
        else if (selected.generated) "Local salvo anteriormente."
        else "Local marcado por você. Visite apenas áreas de acesso público.",
        style = MaterialTheme.typography.bodySmall,
        color = Muted,
    )
    Row {
        TextButton(onClick = { onClose() }) { Text("Fechar") }
        TextButton(
            onClick = {
                onToggleSaved(selected)
                onClose()
            }
        ) {
            Text(if (saved) "Remover dos salvos" else "Salvar local")
        }
    }
}
