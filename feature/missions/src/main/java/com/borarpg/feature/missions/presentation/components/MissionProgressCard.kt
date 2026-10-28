package com.borarpg.feature.missions.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Coral
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Elevated
import com.borarpg.core.designsystem.theme.Ink
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.core.designsystem.theme.Rose
import com.borarpg.domain.model.mission.MissionProgress
import com.borarpg.domain.model.mission.QuestRarity
import com.borarpg.feature.missions.presentation.formatter.label

@Composable
fun MissionProgressCard(
    progress: MissionProgress,
    busy: Boolean,
    onClaim: () -> Unit,
    onCancel: () -> Unit,
) {
    val rarityColor =
        when (progress.quest.rarity) {
            QuestRarity.EPIC -> Rose
            QuestRarity.RARE -> Coral
            QuestRarity.COMMON -> Accent
        }
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Panel,
        border = BorderStroke(1.dp, Cream.copy(alpha = .07f)),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier.size(42.dp)
                        .background(Accent.copy(alpha = .12f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Flag, null, tint = Accent, modifier = Modifier.size(22.dp))
                }
                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        "MISSÃO ATIVA",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    Text(
                        progress.quest.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Cream,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    progress.quest.rarity.label,
                    modifier =
                        Modifier.background(
                                rarityColor.copy(alpha = .12f),
                                RoundedCornerShape(10.dp),
                            )
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = rarityColor,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${progress.steps} / ${progress.quest.goalSteps} passos",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                    Text(
                        "${(progress.fraction * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Accent,
                    )
                }
                LinearProgressIndicator(
                    progress = { progress.fraction },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = Accent,
                    trackColor = Elevated,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MissionReward(Icons.Outlined.AutoAwesome, "+${progress.quest.xpReward} XP", Accent)
                MissionReward(Icons.Outlined.Paid, "${progress.quest.goldReward} ouro", Coral)
            }
            if (progress.quest.itemReward.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Outlined.Inventory2,
                        null,
                        tint = Rose,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        progress.quest.itemReward,
                        color = Cream,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !busy,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Muted.copy(alpha = .3f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Muted),
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = onClaim,
                    enabled = progress.ready && !busy,
                    shape = RoundedCornerShape(14.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Accent,
                            contentColor = Ink,
                            disabledContainerColor = Elevated,
                            disabledContentColor = Muted,
                        ),
                    modifier = Modifier.weight(1f),
                ) {
                    if (busy)
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Muted,
                            strokeWidth = 2.dp,
                        )
                    else Text(if (progress.ready) "Resgatar" else "Em andamento")
                }
            }
        }
    }
}

@Composable
private fun MissionReward(icon: ImageVector, text: String, color: Color) {
    Row(
        Modifier.background(color.copy(alpha = .1f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(15.dp))
        Text(text, color = color, style = MaterialTheme.typography.labelMedium)
    }
}
