package com.borarpg.feature.journey.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Coral
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Elevated
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.feature.journey.presentation.components.ExplorationCard
import com.borarpg.feature.journey.presentation.components.GuardianEncounterCard
import com.borarpg.feature.journey.presentation.components.JourneyMetrics
import com.borarpg.feature.journey.presentation.components.LevelProgressCard
import com.borarpg.feature.journey.presentation.components.NextMissionCard
import com.borarpg.feature.journey.presentation.state.AdventureState
import com.borarpg.feature.missions.presentation.components.MissionProgressCard
import com.borarpg.feature.missions.presentation.state.MissionState

@Composable
internal fun JourneyScreen(
    state: AdventureState,
    name: String,
    startActivity: () -> Unit,
    map: () -> Unit,
    profile: () -> Unit,
    mission: MissionState,
    onClaimMission: () -> Unit,
    onCancelMission: () -> Unit,
) {
    val hero = state.hero
    Column(
        Modifier.fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "BORA RPG",
                    style = MaterialTheme.typography.labelSmall,
                    color = Accent,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (name.isBlank()) "Sua jornada" else "Olá, ${name.substringBefore(' ')}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Cream,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(onClick = profile, shape = CircleShape, color = Elevated) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = "Meu perfil",
                    tint = Cream,
                    modifier = Modifier.padding(13.dp).size(23.dp),
                )
            }
        }
        ExplorationCard(onMap = map, onActivity = startActivity)
        JourneyMetrics(hero)
        LevelProgressCard(hero)
        Text("Missões & encontros", style = MaterialTheme.typography.titleLarge, color = Cream)
        mission.active?.let {
            MissionProgressCard(it, mission.busy, onClaimMission, onCancelMission)
        } ?: NextMissionCard(onMap = map)
        mission.message?.let {
            Text(it, color = Accent, style = MaterialTheme.typography.bodySmall)
        }
        GuardianEncounterCard(hero)
        if (state.loading) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Accent)
                Text("Carregando sua jornada…", color = Muted)
            }
        }
        state.error?.let { Text(it, color = Coral, style = MaterialTheme.typography.bodySmall) }
    }
}
