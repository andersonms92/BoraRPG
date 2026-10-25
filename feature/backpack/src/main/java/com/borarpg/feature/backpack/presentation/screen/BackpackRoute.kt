package com.borarpg.feature.backpack.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borarpg.feature.backpack.presentation.viewmodel.BackpackViewModel
import com.borarpg.feature.missions.presentation.viewmodel.MissionViewModel

@Composable
fun BackpackRoute(missions: MissionViewModel, vm: BackpackViewModel = hiltViewModel()) {
    val hero by vm.hero.collectAsStateWithLifecycle()
    val mission by missions.state.collectAsStateWithLifecycle()
    BackpackScreen(hero, mission.inventory)
}
