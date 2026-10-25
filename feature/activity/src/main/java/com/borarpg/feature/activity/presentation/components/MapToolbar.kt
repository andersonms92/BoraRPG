package com.borarpg.feature.activity.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun MapToolbar(
    onSearchCep: () -> Unit,
    onRecenter: () -> Unit,
    onNewObjectives: () -> Unit,
    activityButton: @Composable () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalButton(onClick = onSearchCep, shape = RoundedCornerShape(18.dp)) {
            Icon(Icons.Outlined.Search, null)
            Spacer(Modifier.width(8.dp))
            Text("Buscar CEP")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            activityButton()
            FilledIconButton(
                onClick = onRecenter,
                modifier = Modifier.size(52.dp),
            ) {
                Icon(Icons.Outlined.MyLocation, "Centralizar na minha posição")
            }
        }
    }
    FilledTonalButton(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(18.dp),
        onClick = onNewObjectives,
    ) {
        Icon(Icons.Outlined.Refresh, null)
        Spacer(Modifier.width(8.dp))
        Text("Novos objetivos")
    }
}
