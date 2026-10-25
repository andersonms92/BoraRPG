package com.borarpg.feature.backpack.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Elevated
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.core.designsystem.theme.Rose
import com.borarpg.domain.model.mission.MissionProgress

@Composable
internal fun MissionLootCard(mission: MissionProgress?) {
    Surface(shape = RoundedCornerShape(24.dp), color = Panel) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.CardGiftcard,
                    null,
                    tint = Rose,
                    modifier = Modifier.size(25.dp),
                )
                Text(
                    "Sua próxima recompensa",
                    style = MaterialTheme.typography.titleMedium,
                    color = Cream,
                )
            }
            if (mission == null) {
                Text(
                    "Aceite uma missão na Atividade. Seus passos rendem XP, ouro e novos itens.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
            } else {
                Text(
                    mission.quest.itemReward,
                    style = MaterialTheme.typography.titleLarge,
                    color = Rose,
                )
                Text(
                    mission.quest.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
                LinearProgressIndicator(
                    progress = { mission.fraction },
                    color = Rose,
                    trackColor = Elevated,
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                )
                Text(
                    if (mission.ready) "Pronto para resgatar na Jornada ou Atividade."
                    else "${mission.steps} de ${mission.quest.goalSteps} passos",
                    style = MaterialTheme.typography.bodySmall,
                    color = Cream,
                )
            }
        }
    }
}
