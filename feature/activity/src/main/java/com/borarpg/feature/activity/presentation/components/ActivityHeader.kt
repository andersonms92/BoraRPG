package com.borarpg.feature.activity.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Elevated
import com.borarpg.core.designsystem.theme.Ink
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.feature.activity.presentation.state.QuestFilter
import com.borarpg.feature.missions.presentation.formatter.label

@Composable
internal fun ActivityHeader(
    objectivesCount: Int,
    filter: QuestFilter,
    onFilter: (QuestFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Ink, Ink.copy(alpha = .94f))))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    "O mundo é sua próxima missão",
                    style = MaterialTheme.typography.labelMedium,
                    color = Muted,
                )
                Text(
                    "Atividade",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Cream,
                )
            }
            Surface(color = Elevated.copy(alpha = .8f), shape = RoundedCornerShape(14.dp)) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Place, null, Modifier.size(15.dp), tint = Accent)
                    Text(
                        if (objectivesCount == 1) "1 local" else "$objectivesCount locais",
                        style = MaterialTheme.typography.labelMedium,
                        color = Cream,
                    )
                }
            }
        }
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuestFilter.entries.forEach { option ->
                val selected = filter == option
                FilterChip(
                    selected = selected,
                    onClick = { onFilter(option) },
                    label = { Text(option.kind?.label ?: "Todos") },
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        FilterChipDefaults.filterChipColors(
                            containerColor = Panel,
                            labelColor = Muted,
                            selectedContainerColor = Accent,
                            selectedLabelColor = Ink,
                        ),
                    border =
                        FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = Elevated,
                            selectedBorderColor = Accent,
                        ),
                )
            }
        }
    }
}
