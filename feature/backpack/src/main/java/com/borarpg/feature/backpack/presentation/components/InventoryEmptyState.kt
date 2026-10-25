package com.borarpg.feature.backpack.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backpack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Elevated
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel

@Composable
internal fun InventoryEmptyState() {
    Surface(shape = RoundedCornerShape(24.dp), color = Panel) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 26.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(shape = RoundedCornerShape(24.dp), color = Elevated) {
                Icon(
                    Icons.Outlined.Backpack,
                    null,
                    tint = Accent,
                    modifier = Modifier.padding(20.dp).size(38.dp),
                )
            }
            Text(
                "Espaço para suas conquistas",
                style = MaterialTheme.typography.titleMedium,
                color = Cream,
            )
            Text(
                "Os itens recebidos nas missões vão aparecer aqui.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                textAlign = TextAlign.Center,
            )
        }
    }
}
