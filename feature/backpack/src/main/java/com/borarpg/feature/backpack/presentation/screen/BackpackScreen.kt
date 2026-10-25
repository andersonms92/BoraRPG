package com.borarpg.feature.backpack.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.components.Eyebrow
import com.borarpg.core.designsystem.components.Tag
import com.borarpg.core.designsystem.illustration.GuardianArt
import com.borarpg.core.designsystem.theme.Coral
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.domain.model.hero.Hero
import com.borarpg.domain.model.inventory.InventoryItem
import com.borarpg.feature.backpack.presentation.components.Equipment

@Composable
internal fun BackpackScreen(hero: Hero, inventory: List<InventoryItem>) {
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
        if (inventory.isEmpty())
            Text("Complete uma missão na Atividade para ganhar itens.", color = Muted)
        inventory.forEach { item ->
            Equipment(
                Icons.Outlined.AutoAwesome,
                item.name,
                "Recompensa de missão",
                "${item.quantity}",
            )
        }
    }
}
