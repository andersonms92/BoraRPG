package com.borarpg.feature.activity.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel

@Composable
internal fun MapToolbar(
    onSearchCep: () -> Unit,
    onRecenter: () -> Unit,
    onNewObjectives: () -> Unit,
    onHistory: () -> Unit,
    activityButton: @Composable () -> Unit,
) {
    val stacked =
        LocalConfiguration.current.screenWidthDp < 360 || LocalDensity.current.fontScale >= 1.2f
    val compact = !stacked && LocalConfiguration.current.screenWidthDp < 440
    val search: @Composable (Modifier) -> Unit = { modifier ->
        Surface(
            modifier = modifier.height(52.dp),
            shape = RoundedCornerShape(18.dp),
            color = Panel.copy(alpha = .97f),
            border = BorderStroke(1.dp, Cream.copy(alpha = .07f)),
            shadowElevation = 8.dp,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = onSearchCep,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Cream),
                ) {
                    Icon(Icons.Outlined.Search, null, Modifier.size(18.dp), tint = Accent)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (compact) "CEP" else "Buscar CEP",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                    )
                }
                VerticalDivider(Modifier.height(20.dp), color = Muted.copy(alpha = .2f))
                IconButton(onClick = onNewObjectives, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Outlined.Refresh, "Novos objetivos", tint = Accent)
                }
            }
        }
    }
    val shortcuts: @Composable () -> Unit = {
        MapShortcut(onClick = onHistory) {
            Icon(Icons.Outlined.History, "Histórico de atividades")
        }
        activityButton()
        MapShortcut(onClick = onRecenter) {
            Icon(Icons.Outlined.MyLocation, "Centralizar na minha posição")
        }
    }
    if (stacked) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            search(Modifier.fillMaxWidth())
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                shortcuts()
            }
        }
    } else {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            search(Modifier.weight(1f))
            shortcuts()
        }
    }
}

@Composable
private fun MapShortcut(onClick: () -> Unit, content: @Composable () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(52.dp),
        shape = RoundedCornerShape(18.dp),
        colors =
            IconButtonDefaults.filledIconButtonColors(
                containerColor = Panel.copy(alpha = .97f),
                contentColor = Cream,
            ),
        content = content,
    )
}
