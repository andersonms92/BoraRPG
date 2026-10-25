package com.borarpg.feature.journey.presentation.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.components.Eyebrow
import com.borarpg.core.designsystem.components.Tag
import com.borarpg.core.designsystem.illustration.GuardianArt
import com.borarpg.core.designsystem.theme.Coral
import com.borarpg.core.designsystem.theme.Ink
import com.borarpg.core.designsystem.theme.Lime
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.feature.journey.presentation.components.JourneyMetric
import com.borarpg.feature.journey.presentation.state.AdventureState
import com.borarpg.feature.missions.presentation.components.MissionProgressCard
import com.borarpg.feature.missions.presentation.state.MissionState

@Composable
internal fun JourneyScreen(
    state: AdventureState,
    name: String,
    startActivity: () -> Unit,
    map: () -> Unit,
    profile: () -> Unit,
    mission: MissionState,
    onClaimMission: () -> Unit,
    onCancelMission: () -> Unit,
) {
    val hero = state.hero
    val hp by animateFloatAsState(hero.hp / 1000f, label = "Vida do guardião")
    Column(
        Modifier.fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Eyebrow("Bora / capítulo 01")
                Text(
                    if (name.isBlank()) "Olá, explorador."
                    else "Olá, ${name.substringBefore(' ')}.",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Surface(onClick = profile, shape = CircleShape, color = Panel) {
                Icon(
                    Icons.Outlined.Person,
                    "Meu perfil",
                    tint = Lime,
                    modifier = Modifier.padding(14.dp),
                )
            }
        }
        Column {
            Text("Seu próximo\npasso é uma", style = MaterialTheme.typography.displayLarge)
            Text("aventura.", style = MaterialTheme.typography.displayLarge, color = Lime)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            JourneyMetric("NÍVEL", "${hero.level}", Modifier.weight(1f))
            JourneyMetric("PASSOS", "${hero.steps}", Modifier.weight(1f))
            JourneyMetric("OURO", "${hero.gold}", Modifier.weight(1f))
        }
        Button(onClick = startActivity, modifier = Modifier.fillMaxWidth()) {
            Text("Abrir atividade · caminhar ou correr")
        }
        mission.active?.let {
            MissionProgressCard(it, mission.busy, onClaimMission, onCancelMission)
        }
        mission.message?.let { Text(it, color = Lime) }
        Box(
            Modifier.fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 8.dp,
                        topEnd = 40.dp,
                        bottomEnd = 8.dp,
                        bottomStart = 40.dp,
                    )
                )
                .background(
                    Brush.verticalGradient(
                        listOf(Panel, androidx.compose.ui.graphics.Color(0xFF293D2C))
                    )
                )
        ) {
            GuardianArt(Modifier.align(Alignment.TopEnd).fillMaxWidth(.82f).height(270.dp))
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Tag("ENCONTRO ATIVO", Coral)
                Spacer(Modifier.height(130.dp))
                Eyebrow("Floresta do recomeço")
                Text("O guardião\ndas raízes", style = MaterialTheme.typography.headlineLarge)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "VIDA DO GUARDIÃO",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    Text("${hero.hp} / 1.000", style = MaterialTheme.typography.labelSmall)
                }
                LinearProgressIndicator(
                    progress = { hp },
                    color = Coral,
                    trackColor = Ink,
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                )
                Text(
                    "Cada passo registrado durante sua atividade causa um ponto de dano.",
                    color = Muted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tag("+50 XP")
                    Tag("25 OURO")
                    Tag("1 ITEM")
                }
            }
        }
        Surface(onClick = map, shape = RoundedCornerShape(26.dp), color = Lime) {
            Row(
                Modifier.fillMaxWidth().padding(22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Eyebrow("Leve a aventura para fora", Ink)
                    Text(
                        "Explorar território",
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink,
                    )
                }
                Icon(
                    Icons.Outlined.NorthEast,
                    "Abrir mapa",
                    tint = Ink,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Eyebrow("Sua evolução")
                Text("${hero.xp} XP conquistados", style = MaterialTheme.typography.titleLarge)
            }
            Text("${hero.victories}", style = MaterialTheme.typography.headlineLarge, color = Coral)
        }
        LinearProgressIndicator(
            progress = { hero.xp % 100 / 100f },
            modifier = Modifier.fillMaxWidth(),
            color = Lime,
            trackColor = Panel,
        )
        Text(
            "${100 - hero.xp % 100} XP até o nível ${hero.level + 1} · ${hero.victories} vitórias",
            color = Muted,
        )
        if (state.loading) CircularProgressIndicator()
        state.error?.let { Text(it, color = Coral) }
    }
}
