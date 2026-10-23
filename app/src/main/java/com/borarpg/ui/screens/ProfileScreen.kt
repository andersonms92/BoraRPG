package com.borarpg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borarpg.domain.Hero
import com.borarpg.ui.PlayerViewModel
import com.borarpg.ui.theme.*

@Composable
internal fun ProfileScreen(vm: PlayerViewModel, hero: Hero) {
    val state by vm.profile.collectAsStateWithLifecycle()
    val draft = state.draft
    Column(
        Modifier.fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Eyebrow("Seu ponto de partida")
        Text("A aventura\ntem seu ritmo.", style = MaterialTheme.typography.headlineLarge)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                Modifier.size(72.dp).background(Lime, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    draft.name.firstOrNull()?.uppercase() ?: "B",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Ink,
                )
            }
            Column {
                Text(
                    draft.name.ifBlank { "Seu explorador" },
                    style = MaterialTheme.typography.titleLarge,
                )
                Text("Nível ${hero.level} · ${hero.xp} XP", color = Muted)
            }
        }
        HorizontalDivider(color = Panel)
        Text("Seus dados", style = MaterialTheme.typography.titleLarge)
        Text(
            "Guarde suas medidas para acompanhar sua jornada. Você pode editar quando quiser.",
            color = Muted,
        )
        if (state.loading) CircularProgressIndicator()
        else {
            ProfileField(
                "Nome",
                draft.name,
                state.errors["name"],
                KeyboardType.Text,
                !state.saving,
            ) {
                vm.edit(draft.copy(name = it.take(40)))
            }
            ProfileField(
                "Peso",
                draft.weight,
                state.errors["weight"],
                KeyboardType.Decimal,
                !state.saving,
                "kg",
            ) {
                vm.edit(draft.copy(weight = it.take(7)))
            }
            ProfileField(
                "Altura",
                draft.height,
                state.errors["height"],
                KeyboardType.Decimal,
                !state.saving,
                "cm",
            ) {
                vm.edit(draft.copy(height = it.take(6)))
            }
            ProfileField(
                "Idade",
                draft.age,
                state.errors["age"],
                KeyboardType.Number,
                !state.saving,
                "anos",
            ) {
                vm.edit(draft.copy(age = it.take(3)))
            }
            Button(
                onClick = vm::save,
                enabled = !state.saving,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text(
                    if (state.saving) "Salvando…" else "Salvar meu perfil",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        state.message?.let { Text(it, color = if (it.startsWith("Perfil salvo")) Lime else Coral) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.Lock, null, tint = Muted, modifier = Modifier.size(18.dp))
            Text(
                "Seus dados ficam neste aparelho. Peso, altura e idade ainda não alteram os treinos ou as recompensas.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    error: String?,
    keyboard: KeyboardType,
    enabled: Boolean,
    unit: String? = null,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        suffix = unit?.let { { Text(it, color = Muted) } },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
    )
}
