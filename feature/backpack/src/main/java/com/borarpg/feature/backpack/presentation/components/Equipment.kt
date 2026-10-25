package com.borarpg.feature.backpack.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.domain.model.inventory.InventoryItem

@Composable
internal fun Equipment(item: InventoryItem, accent: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(22.dp), color = Panel) {
        Column(
            Modifier.fillMaxWidth().heightIn(min = 180.dp).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = .13f)) {
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        null,
                        tint = accent,
                        modifier = Modifier.padding(12.dp).size(24.dp),
                    )
                }
                Text("×${item.quantity}", color = accent, fontWeight = FontWeight.Bold)
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = Cream,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text("Item de missão", style = MaterialTheme.typography.labelSmall, color = Muted)
            }
        }
    }
}
