package com.borarpg.feature.backpack.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.core.designsystem.theme.Rose
import java.text.NumberFormat

@Composable
internal fun ForestFragmentsCard(count: Int) {
    Surface(shape = RoundedCornerShape(24.dp), color = Panel) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(Icons.Outlined.Diamond, null, tint = Rose, modifier = Modifier.size(30.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    "Fragmentos da floresta",
                    style = MaterialTheme.typography.titleSmall,
                    color = Cream,
                )
                Text(
                    "Vitórias contra o guardião",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
            Text(
                NumberFormat.getIntegerInstance().format(count),
                style = MaterialTheme.typography.headlineSmall,
                color = Rose,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
