package com.borarpg.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borarpg.ui.map.WorldMap
import com.borarpg.ui.screens.*
import com.borarpg.ui.theme.*

@Composable
fun AdventureApp(
    adventure: AdventureViewModel,
    player: PlayerViewModel,
    missions: MissionViewModel,
) {
    val state by adventure.state.collectAsStateWithLifecycle()
    val profile by player.profile.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    BoraTheme {
        Scaffold(
            containerColor = Ink,
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                Surface(color = Ink, border = BorderStroke(1.dp, Panel)) {
                    Row(
                        Modifier.fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        val names = listOf("Jornada", "Território", "Mochila", "Você")
                        val icons =
                            listOf(
                                Icons.Outlined.Explore,
                                Icons.Outlined.Map,
                                Icons.Outlined.Backpack,
                                Icons.Outlined.Person,
                            )
                        names.forEachIndexed { index, name ->
                            Column(
                                Modifier.weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { tab = index }
                                    .padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    icons[index],
                                    name,
                                    tint = if (tab == index) Lime else Muted,
                                    modifier = Modifier.size(23.dp),
                                )
                                Text(
                                    name,
                                    fontSize = 11.sp,
                                    fontWeight =
                                        if (tab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (tab == index) Lime else Muted,
                                )
                                Box(
                                    Modifier.size(
                                            width = if (tab == index) 18.dp else 0.dp,
                                            height = 3.dp,
                                        )
                                        .background(Lime, CircleShape)
                                )
                            }
                        }
                    }
                }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                AnimatedContent(targetState = tab, label = "Navegação") { page ->
                    when (page) {
                        0 ->
                            JourneyScreen(
                                state,
                                profile.draft.name,
                                adventure::startActivity,
                                adventure::stopActivity,
                                { tab = 1 },
                                { tab = 3 },
                                missions,
                            )
                        1 -> WorldMap(player, missions)
                        2 -> BackpackScreen(state.hero, missions)
                        3 -> ProfileScreen(player, state.hero)
                    }
                }
            }
        }
    }
}
