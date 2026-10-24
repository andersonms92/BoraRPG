package com.borarpg.feature.missions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.*
import com.borarpg.domain.MissionProgress

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
