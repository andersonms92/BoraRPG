package com.borarpg.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.borarpg.core.designsystem.*
import com.borarpg.feature.activity.WorldMap
import com.borarpg.feature.backpack.BackpackRoute
import com.borarpg.feature.journey.JourneyRoute
import com.borarpg.feature.missions.MissionViewModel
import com.borarpg.feature.profile.ProfileRoute

@Composable
fun AdventureApp() {
    val missions: MissionViewModel = hiltViewModel()
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
                        val names = listOf("Jornada", "Atividade", "Mochila", "Você")
                        val icons =
                            listOf(
                                Icons.Outlined.Explore,
                                Icons.AutoMirrored.Outlined.DirectionsRun,
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
                        0 -> JourneyRoute(missions, { tab = 1 }, { tab = 3 })
                        1 -> WorldMap(missions = missions)
                        2 -> BackpackRoute(missions)
                        3 -> ProfileRoute()
                    }
                }
            }
        }
    }
}
