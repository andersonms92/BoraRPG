package com.borarpg.feature.profile.presentation.screen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.components.Eyebrow
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Coral
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Ink
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.core.designsystem.theme.Rose
import com.borarpg.domain.model.hero.Hero
import com.borarpg.feature.profile.presentation.components.ProfileField
import com.borarpg.feature.profile.presentation.components.ProfileIdentityCard
import com.borarpg.feature.profile.presentation.components.ProfileSection
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
            .background(Ink)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Eyebrow("Sua identidade", Muted)
            Text("Feito para você.", style = MaterialTheme.typography.headlineLarge, color = Cream)
            Text(
                "Cada passo tem a sua história.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
        }
        ProfileIdentityCard(draft.name, hero)
        if (state.loading) {
            Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent, modifier = Modifier.size(28.dp))
            }
        } else {
            ProfileSection(
                title = "Dados pessoais",
                description = "Como você quer ser conhecido.",
                icon = Icons.Outlined.PersonOutline,
                accent = Coral,
            ) {
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
                    "Idade",
                    draft.age,
                    state.errors["age"],
                    KeyboardType.Number,
                    !state.saving,
                    "anos",
                ) {
                    onEdit(draft.copy(age = it.take(3)))
                }
            }
            ProfileSection(
                title = "Suas medidas",
                description = "Para estimar as calorias das atividades.",
                icon = Icons.Outlined.Straighten,
                accent = Rose,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
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
                    }
                    Box(Modifier.weight(1f)) {
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
                    }
                }
            }
            Button(
                onClick = onSave,
                enabled = !state.saving,
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Ink),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().height(58.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (state.saving)
                        CircularProgressIndicator(
                            Modifier.size(18.dp),
                            color = Muted,
                            strokeWidth = 2.dp,
                        )
                    else Icon(Icons.Outlined.Check, null, modifier = Modifier.size(20.dp))
                    Text(
                        if (state.saving) "Salvando…" else "Salvar meu perfil",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        state.message?.let { message ->
            val success = message.startsWith("Perfil salvo")
            val color = if (success) Accent else Rose
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = color.copy(alpha = .09f),
                border = BorderStroke(1.dp, color.copy(alpha = .2f)),
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (success) Icons.Outlined.CheckCircleOutline
                        else Icons.Outlined.ErrorOutline,
                        null,
                        tint = color,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(message, color = Cream, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Surface(shape = RoundedCornerShape(20.dp), color = Panel.copy(alpha = .6f)) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Outlined.Shield,
                    null,
                    tint = Muted,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    "Seus dados ficam neste aparelho. O peso é usado para estimar as calorias das atividades.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }
    }
}
