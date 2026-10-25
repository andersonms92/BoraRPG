package com.borarpg.feature.activity.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.components.Eyebrow
import com.borarpg.core.designsystem.components.Tag
import com.borarpg.core.designsystem.theme.Ink

@Composable
internal fun ActivityHeader(
    objectivesCount: Int,
    filter: String,
    onFilter: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Ink.copy(alpha = .94f))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Eyebrow("O mundo é sua próxima missão")
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Atividade", style = MaterialTheme.typography.headlineLarge)
            Tag("${objectivesCount} LOCAIS")
        }
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("Todos", "Combate", "Exploração").forEach { label ->
                FilterChip(
                    selected = filter == label,
                    onClick = {
                        onFilter(label)
                    },
                    label = { Text(label) },
                )
            }
        }
    }
}
