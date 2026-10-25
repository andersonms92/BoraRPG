package com.borarpg.feature.journey.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Elevated
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.core.designsystem.theme.Rose
import com.borarpg.domain.model.hero.Hero
import com.borarpg.domain.rules.combat.Combat
import java.text.NumberFormat

@Composable
internal fun GuardianEncounterCard(hero: Hero) {
    val numbers = NumberFormat.getIntegerInstance()
    val victories =
        if (hero.victories == 1) "1 guardião derrotado"
        else "${numbers.format(hero.victories)} guardiões derrotados"
    val hp by animateFloatAsState(hero.hp / 1000f, label = "Vida do guardião")
    Surface(shape = RoundedCornerShape(24.dp), color = Panel) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = RoundedCornerShape(14.dp), color = Rose.copy(alpha = .14f)) {
                    Icon(
                        Icons.Outlined.Shield,
                        null,
                        tint = Rose,
                        modifier = Modifier.padding(12.dp).size(24.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "GUARDIÃO Nº ${numbers.format(hero.victories.toLong() + 1)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Rose,
                    )
                    Text(
                        "Guardião das raízes",
                        style = MaterialTheme.typography.titleMedium,
                        color = Cream,
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Vida do guardião", style = MaterialTheme.typography.bodySmall, color = Muted)
                Text(
                    "${hero.hp} / 1.000",
                    style = MaterialTheme.typography.labelMedium,
                    color = Cream,
                    fontWeight = FontWeight.Bold,
                )
            }
            LinearProgressIndicator(
                progress = { hp },
                color = Rose,
                trackColor = Elevated,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            )
            Text(
                "$victories · ${numbers.format(hero.steps)} passos registrados",
                color = Accent,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                "Cada passo na atividade causa 1 de dano. Ao derrotar um guardião, outro surge com 1.000 de vida.",
                color = Muted,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "+${Combat.VICTORY_XP} XP · ${Combat.VICTORY_GOLD} ouro · 1 fragmento por vitória",
                style = MaterialTheme.typography.labelMedium,
                color = Accent,
            )
        }
    }
}
