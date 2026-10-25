package com.borarpg.feature.journey.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.core.designsystem.theme.Rose

@Composable
internal fun NextMissionCard(onMap: () -> Unit) {
    Surface(onClick = onMap, shape = RoundedCornerShape(24.dp), color = Panel) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(Icons.Outlined.Flag, null, tint = Rose, modifier = Modifier.size(28.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Sua próxima missão",
                    style = MaterialTheme.typography.titleMedium,
                    color = Cream,
                )
                Text(
                    "Escolha um objetivo no mapa.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
            Icon(
                Icons.Outlined.NorthEast,
                "Explorar missões",
                tint = Muted,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
