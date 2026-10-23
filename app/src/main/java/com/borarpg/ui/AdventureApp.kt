package com.borarpg.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borarpg.domain.*

@Composable fun AdventureApp(adventure: AdventureViewModel, player: PlayerViewModel) {
    val state by adventure.state.collectAsStateWithLifecycle()
    val profile by player.profile.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    BoraTheme {
        Scaffold(containerColor = Ink, contentWindowInsets = WindowInsets(0), bottomBar = {
            Surface(color = Ink, border = BorderStroke(1.dp, Panel)) {
                Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    val names = listOf("Jornada", "Território", "Mochila", "Você")
                    val icons = listOf(Icons.Outlined.Explore, Icons.Outlined.Map, Icons.Outlined.Backpack, Icons.Outlined.Person)
                    names.forEachIndexed { index, name ->
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).clickable { tab = index }.padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(icons[index], name, tint = if (tab == index) Lime else Muted, modifier = Modifier.size(23.dp))
                            Text(name, fontSize = 11.sp, fontWeight = if (tab == index) FontWeight.Bold else FontWeight.Normal, color = if (tab == index) Lime else Muted)
                            Box(Modifier.size(width = if (tab == index) 18.dp else 0.dp, height = 3.dp).background(Lime, CircleShape))
                        }
                    }
                }
            }
        }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                AnimatedContent(targetState = tab, label = "Navegação") { page ->
                    when (page) {
                        0 -> Journey(state, profile.draft.name, adventure::simulate, { tab = 1 }, { tab = 3 })
                        1 -> MapPreview()
                        2 -> Backpack(state.hero)
                        3 -> PlayerScreen(player, state.hero)
                    }
                }
            }
        }
    }
}

@Composable private fun Journey(state: AdventureState, name: String, simulate: () -> Unit, map: () -> Unit, profile: () -> Unit) {
    var showLab by rememberSaveable { mutableStateOf(false) }
    val hero = state.hero
    val hp by animateFloatAsState(hero.hp / 1000f, label = "Vida do guardião")
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Eyebrow("Bora / capítulo 01"); Text(if (name.isBlank()) "Olá, explorador." else "Olá, ${name.substringBefore(' ')}.", style = MaterialTheme.typography.titleLarge) }
            Surface(onClick = profile, shape = CircleShape, color = Panel) { Icon(Icons.Outlined.Person, "Meu perfil", tint = Lime, modifier = Modifier.padding(14.dp)) }
        }
        Column {
            Text("Seu próximo\npasso é uma", style = MaterialTheme.typography.displayLarge)
            Text("aventura.", style = MaterialTheme.typography.displayLarge, color = Lime)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Metric("NÍVEL", "${hero.level}", Modifier.weight(1f))
            Metric("PASSOS TESTE", "${hero.steps}", Modifier.weight(1f))
            Metric("OURO", "${hero.gold}", Modifier.weight(1f))
        }
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 8.dp, topEnd = 40.dp, bottomEnd = 8.dp, bottomStart = 40.dp)).background(Brush.verticalGradient(listOf(Panel, androidx.compose.ui.graphics.Color(0xFF293D2C))))) {
            GuardianArt(Modifier.align(Alignment.TopEnd).fillMaxWidth(.82f).height(270.dp))
            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Tag("ENCONTRO ATIVO", Coral)
                Spacer(Modifier.height(130.dp))
                Eyebrow("Floresta do recomeço")
                Text("O guardião\ndas raízes", style = MaterialTheme.typography.headlineLarge)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("VIDA DO GUARDIÃO", style = MaterialTheme.typography.labelSmall, color = Muted)
                    Text("${hero.hp} / 1.000", style = MaterialTheme.typography.labelSmall)
                }
                LinearProgressIndicator(progress = { hp }, color = Coral, trackColor = Ink, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape))
                Text("Continue de onde parou. Cada passo de teste causa um ponto de dano.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Tag("+50 XP"); Tag("25 OURO"); Tag("1 ITEM") }
            }
        }
        Surface(onClick = map, shape = RoundedCornerShape(26.dp), color = Lime) {
            Row(Modifier.fillMaxWidth().padding(22.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Eyebrow("Leve a aventura para fora", Ink); Text("Explorar território", style = MaterialTheme.typography.titleLarge, color = Ink) }
                Icon(Icons.Outlined.NorthEast, "Abrir mapa", tint = Ink, modifier = Modifier.size(32.dp))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Eyebrow("Sua evolução"); Text("${hero.xp} XP conquistados", style = MaterialTheme.typography.titleLarge) }
            Text("${hero.victories}", style = MaterialTheme.typography.headlineLarge, color = Coral)
        }
        LinearProgressIndicator(progress = { hero.xp % 100 / 100f }, modifier = Modifier.fillMaxWidth(), color = Lime, trackColor = Panel)
        Text("${100 - hero.xp % 100} XP até o nível ${hero.level + 1} · ${hero.victories} vitórias", color = Muted)
        HorizontalDivider(color = Panel)
        TextButton(onClick = { showLab = !showLab }) { Icon(Icons.Outlined.Science, null); Spacer(Modifier.width(8.dp)); Text(if (showLab) "Fechar laboratório" else "Laboratório do protótipo") }
        if (showLab) {
            Text("O GPS do mapa é real. O combate ainda usa passos simulados; caminhada, corrida e bicicleta não são gravadas nesta versão.", color = Muted)
            Button(enabled = !state.loading, onClick = simulate, modifier = Modifier.fillMaxWidth()) { Text("Simular 250 passos") }
        }
        if (state.loading) CircularProgressIndicator()
        state.error?.let { Text(it, color = Coral) }
    }
}

@Composable private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Eyebrow(label, Muted)
        Text(value, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Cream)
    }
}

@Composable private fun Backpack(hero: Hero) {
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Eyebrow("Seu equipamento")
        Text("Preparado para\no próximo passo.", style = MaterialTheme.typography.headlineLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Tag("NÍVEL ${hero.level}"); Tag("${hero.gold} OURO", Coral) }
        GuardianArt(Modifier.fillMaxWidth().height(220.dp).background(Panel, RoundedCornerShape(28.dp)))
        Equipment(Icons.Outlined.Bolt, "Espada de madeira", "Equipamento inicial · sem bônus de dano", "01")
        Equipment(Icons.Outlined.AutoAwesome, "Fragmentos da floresta", "Obtidos ao derrotar o guardião", "${hero.victories}")
        Text("Sua mochila cresce a cada encontro. Loja e habilidades de equipamentos chegam em uma próxima etapa.", color = Muted)
    }
}

@Composable private fun Equipment(icon: ImageVector, title: String, detail: String, count: String) {
    Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(22.dp)).padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(icon, null, tint = Lime, modifier = Modifier.size(28.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(detail, color = Muted, style = MaterialTheme.typography.bodySmall) }
        Text(count, style = MaterialTheme.typography.titleLarge, color = Lime)
    }
}

@Composable private fun PlayerScreen(vm: PlayerViewModel, hero: Hero) {
    val state by vm.profile.collectAsStateWithLifecycle()
    val draft = state.draft
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).imePadding().padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Eyebrow("Seu ponto de partida")
        Text("A aventura\ntem seu ritmo.", style = MaterialTheme.typography.headlineLarge)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.size(72.dp).background(Lime, RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                Text(draft.name.firstOrNull()?.uppercase() ?: "B", style = MaterialTheme.typography.headlineLarge, color = Ink)
            }
            Column { Text(draft.name.ifBlank { "Seu explorador" }, style = MaterialTheme.typography.titleLarge); Text("Nível ${hero.level} · ${hero.xp} XP", color = Muted) }
        }
        HorizontalDivider(color = Panel)
        Text("Seus dados", style = MaterialTheme.typography.titleLarge)
        Text("Guarde suas medidas para acompanhar sua jornada. Você pode editar quando quiser.", color = Muted)
        if (state.loading) CircularProgressIndicator() else {
            ProfileField("Nome", draft.name, state.errors["name"], KeyboardType.Text, !state.saving) { vm.edit(draft.copy(name = it.take(40))) }
            ProfileField("Peso", draft.weight, state.errors["weight"], KeyboardType.Decimal, !state.saving, "kg") { vm.edit(draft.copy(weight = it.take(7))) }
            ProfileField("Altura", draft.height, state.errors["height"], KeyboardType.Decimal, !state.saving, "cm") { vm.edit(draft.copy(height = it.take(6))) }
            ProfileField("Idade", draft.age, state.errors["age"], KeyboardType.Number, !state.saving, "anos") { vm.edit(draft.copy(age = it.take(3))) }
            Button(onClick = vm::save, enabled = !state.saving, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text(if (state.saving) "Salvando…" else "Salvar meu perfil", fontWeight = FontWeight.Bold)
            }
        }
        state.message?.let { Text(it, color = if (it.startsWith("Perfil salvo")) Lime else Coral) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.Lock, null, tint = Muted, modifier = Modifier.size(18.dp))
            Text("Seus dados ficam neste aparelho. Peso, altura e idade ainda não alteram os treinos ou as recompensas.", style = MaterialTheme.typography.bodySmall, color = Muted)
        }
    }
}

@Composable private fun ProfileField(label: String, value: String, error: String?, keyboard: KeyboardType, enabled: Boolean, unit: String? = null, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) }, suffix = unit?.let { { Text(it, color = Muted) } }, singleLine = true,
        isError = error != null, supportingText = error?.let { { Text(it) } }, enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth())
}

@Composable private fun MapPreview() { Text("Território em preparação.") }
