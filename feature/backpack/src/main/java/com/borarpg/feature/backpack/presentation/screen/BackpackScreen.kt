package com.borarpg.feature.backpack.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.core.designsystem.theme.Rose
import com.borarpg.domain.model.hero.Hero
import com.borarpg.domain.model.inventory.InventoryItem
import com.borarpg.domain.model.mission.MissionProgress
import com.borarpg.feature.backpack.presentation.components.Equipment
import com.borarpg.feature.backpack.presentation.components.ForestFragmentsCard
import com.borarpg.feature.backpack.presentation.components.InventoryEmptyState
import com.borarpg.feature.backpack.presentation.components.MissionLootCard
import java.text.NumberFormat

@Composable
internal fun BackpackScreen(
    hero: Hero,
    inventory: List<InventoryItem>,
    activeMission: MissionProgress?,
) {
    val numbers = NumberFormat.getIntegerInstance()
    Column(
        Modifier.fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("SEU PERSONAGEM", style = MaterialTheme.typography.labelSmall, color = Accent)
            Text("Mochila", style = MaterialTheme.typography.headlineLarge, color = Cream)
            Text(
                "Cada conquista vem com você.",
                color = Muted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Surface(shape = RoundedCornerShape(24.dp), color = Panel) {
            Row(
                Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(shape = RoundedCornerShape(18.dp), color = Accent.copy(alpha = .13f)) {
                    Icon(
                        Icons.Outlined.MonetizationOn,
                        null,
                        tint = Accent,
                        modifier = Modifier.padding(15.dp).size(28.dp),
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Ouro conquistado",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                    Text(
                        numbers.format(hero.gold),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Cream,
                    )
                }
                Text(
                    "NÍVEL ${hero.level}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Accent,
                )
            }
        }
        ForestFragmentsCard(hero.victories)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Itens de missão", style = MaterialTheme.typography.titleLarge, color = Cream)
            Text(
                "${numbers.format(inventory.sumOf { it.quantity })} itens",
                style = MaterialTheme.typography.labelMedium,
                color = Muted,
            )
        }
        if (inventory.isEmpty()) InventoryEmptyState()
        else {
            inventory.chunked(2).forEachIndexed { rowIndex, row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEachIndexed { columnIndex, item ->
                        Equipment(
                            item = item,
                            accent = if ((rowIndex * 2 + columnIndex) % 2 == 0) Accent else Rose,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        MissionLootCard(activeMission)
    }
}
