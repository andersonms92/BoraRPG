package com.borarpg.feature.missions.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.components.Eyebrow
import com.borarpg.core.designsystem.theme.Lime
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.domain.model.mission.MissionProgress

@Composable
fun MissionProgressCard(
    progress: MissionProgress,
    busy: Boolean,
    onClaim: () -> Unit,
    onCancel: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(22.dp), color = Panel) {
        Column(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Eyebrow("Missão ativa · ${progress.quest.rarity}")
            Text(progress.quest.title, style = MaterialTheme.typography.titleLarge)
            Text("${progress.steps} / ${progress.quest.goalSteps} passos", color = Muted)
            LinearProgressIndicator(
                progress = { progress.fraction },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "+${progress.quest.xpReward} XP · ${progress.quest.goldReward} ouro · ${progress.quest.itemReward}",
                color = Lime,
            )
            Button(
                onClick = onClaim,
                enabled = progress.ready && !busy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (progress.ready) "Resgatar recompensas" else "Cumpra a meta de passos")
            }
            TextButton(onClick = onCancel, enabled = !busy) { Text("Cancelar missão") }
        }
    }
}
