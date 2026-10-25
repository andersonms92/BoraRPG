package com.borarpg.feature.journey.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borarpg.feature.journey.presentation.viewmodel.AdventureViewModel
import com.borarpg.feature.missions.presentation.viewmodel.MissionViewModel

@Composable
fun JourneyRoute(
    missions: MissionViewModel,
    onActivity: () -> Unit,
    onProfile: () -> Unit,
    vm: AdventureViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val name by vm.name.collectAsStateWithLifecycle()
    val mission by missions.state.collectAsStateWithLifecycle()
    JourneyScreen(
        state = state,
        name = name,
        startActivity = onActivity,
        map = onActivity,
        profile = onProfile,
        mission = mission,
        onClaimMission = missions::claim,
        onCancelMission = missions::cancel,
    )
}
