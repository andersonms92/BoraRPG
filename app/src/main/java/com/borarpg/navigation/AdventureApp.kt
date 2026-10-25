package com.borarpg.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.borarpg.core.designsystem.theme.BoraTheme
import com.borarpg.core.designsystem.theme.Ink
import com.borarpg.feature.activity.presentation.screen.ActivityRoute
import com.borarpg.feature.backpack.presentation.screen.BackpackRoute
import com.borarpg.feature.journey.presentation.screen.JourneyRoute
import com.borarpg.feature.missions.presentation.viewmodel.MissionViewModel
import com.borarpg.feature.profile.presentation.screen.ProfileRoute

@Composable
fun AdventureApp() {
    val missions: MissionViewModel = hiltViewModel()
    var tab by rememberSaveable { mutableStateOf(AppDestination.JOURNEY) }
    BoraTheme {
        Scaffold(
            containerColor = Ink,
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                AppNavigationBar(selected = tab, onSelect = { tab = it })
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                AnimatedContent(targetState = tab, label = "Navegação") { page ->
                    when (page) {
                        AppDestination.JOURNEY ->
                            JourneyRoute(
                                missions,
                                { tab = AppDestination.ACTIVITY },
                                { tab = AppDestination.PROFILE },
                            )
                        AppDestination.ACTIVITY -> ActivityRoute(missions = missions)
                        AppDestination.BACKPACK -> BackpackRoute(missions)
                        AppDestination.PROFILE -> ProfileRoute()
                    }
                }
            }
        }
    }
}
