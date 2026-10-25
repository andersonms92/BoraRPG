package com.borarpg.feature.profile.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.components.Eyebrow
import com.borarpg.core.designsystem.theme.Coral
import com.borarpg.core.designsystem.theme.Ink
import com.borarpg.core.designsystem.theme.Lime
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.domain.model.hero.Hero
import com.borarpg.feature.profile.presentation.components.ProfileField
import com.borarpg.feature.profile.presentation.form.ProfileForm
import com.borarpg.feature.profile.presentation.state.ProfileState

@Composable
internal fun ProfileScreen(
    state: ProfileState,
    hero: Hero,
    onEdit: (ProfileForm) -> Unit,
    onSave: () -> Unit,
) {
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
                onEdit(draft.copy(name = it.take(40)))
            }
            ProfileField(
                "Peso",
                draft.weight,
                state.errors["weight"],
                KeyboardType.Decimal,
                !state.saving,
                "kg",
            ) {
                onEdit(draft.copy(weight = it.take(7)))
            }
            ProfileField(
                "Altura",
                draft.height,
                state.errors["height"],
                KeyboardType.Decimal,
                !state.saving,
                "cm",
            ) {
                onEdit(draft.copy(height = it.take(6)))
            }
            ProfileField(
                "Idade",
                draft.age,
                state.errors["age"],
                KeyboardType.Number,
                !state.saving,
                "anos",
            ) {
                onEdit(draft.copy(age = it.take(3)))
            }
            Button(
                onClick = onSave,
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
                "Seus dados ficam neste aparelho. O peso é usado para estimar as calorias das atividades.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        }
    }
}
