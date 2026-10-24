package com.borarpg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borarpg.domain.Hero
import com.borarpg.ui.MissionViewModel
import com.borarpg.ui.components.GuardianArt
import com.borarpg.ui.theme.*

@Composable
internal fun BackpackScreen(hero: Hero, missions: MissionViewModel) {
    val mission by missions.state.collectAsStateWithLifecycle()
    Column(
        Modifier.fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Eyebrow("Seu equipamento")
        Text("Preparado para\no próximo passo.", style = MaterialTheme.typography.headlineLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Tag("NÍVEL ${hero.level}")
            Tag("${hero.gold} OURO", Coral)
        }
        GuardianArt(
            Modifier.fillMaxWidth().height(220.dp).background(Panel, RoundedCornerShape(28.dp))
        )
        Equipment(
            Icons.Outlined.Bolt,
            "Espada de madeira",
            "Equipamento inicial · sem bônus de dano",
            "01",
        )
        Equipment(
            Icons.Outlined.AutoAwesome,
            "Fragmentos da floresta",
            "Obtidos ao derrotar o guardião",
            "${hero.victories}",
        )
        Text(
            "Itens de missões",
            color = Muted,
        )
        if (mission.inventory.isEmpty())
            Text("Complete uma missão na Atividade para ganhar itens.", color = Muted)
        mission.inventory.forEach { item ->
            Equipment(
                Icons.Outlined.AutoAwesome,
                item.name,
                "Recompensa de missão",
                "${item.quantity}",
            )
        }
    }
}

@Composable
private fun Equipment(icon: ImageVector, title: String, detail: String, count: String) {
    Row(
        Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(22.dp)).padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(icon, null, tint = Lime, modifier = Modifier.size(28.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(detail, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        Text(count, style = MaterialTheme.typography.titleLarge, color = Lime)
    }
}
