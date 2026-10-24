package com.borarpg.feature.journey

import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borarpg.feature.missions.MissionViewModel

@Composable
fun JourneyRoute(
    missions: MissionViewModel,
    onActivity: () -> Unit,
    onProfile: () -> Unit,
    vm: AdventureViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val name by vm.name.collectAsStateWithLifecycle()
    JourneyScreen(state, name, onActivity, onActivity, onProfile, missions)
}
