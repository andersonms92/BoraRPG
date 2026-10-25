package com.borarpg.feature.journey.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Elevated
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.domain.model.hero.Hero
import java.text.NumberFormat

@Composable
internal fun LevelProgressCard(hero: Hero) {
    val numbers = NumberFormat.getIntegerInstance()
    Surface(shape = RoundedCornerShape(24.dp), color = Panel) {
        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { hero.levelProgress },
                    modifier = Modifier.size(64.dp),
                    color = Accent,
                    trackColor = Elevated,
                    strokeWidth = 5.dp,
                )
                Text(
                    "${hero.level}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Cream,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Seu personagem", style = MaterialTheme.typography.labelMedium, color = Muted)
                Text(
                    "${numbers.format(hero.xp)} XP conquistados",
                    style = MaterialTheme.typography.titleMedium,
                    color = Cream,
                )
                Text(
                    "${numbers.format(hero.xpInLevel)} / ${numbers.format(hero.xpForNextLevel)} XP neste nível",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
                Text(
                    "${numbers.format(hero.xpToNextLevel)} XP para o nível ${hero.level + 1}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Accent,
                )
            }
        }
    }
}
