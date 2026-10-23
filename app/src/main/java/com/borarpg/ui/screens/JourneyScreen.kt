package com.borarpg.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borarpg.ui.AdventureState
import com.borarpg.ui.components.ActivityControl
import com.borarpg.ui.components.GuardianArt
import com.borarpg.ui.theme.*

@Composable
internal fun JourneyScreen(
    state: AdventureState,
    name: String,
    startActivity: () -> Unit,
    stopActivity: () -> Unit,
    map: () -> Unit,
    profile: () -> Unit,
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
            Metric("NÍVEL", "${hero.level}", Modifier.weight(1f))
            Metric("PASSOS", "${hero.steps}", Modifier.weight(1f))
            Metric("OURO", "${hero.gold}", Modifier.weight(1f))
        }
        ActivityControl(state, startActivity, stopActivity)
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

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Eyebrow(label, Muted)
        Text(value, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Cream)
    }
}
