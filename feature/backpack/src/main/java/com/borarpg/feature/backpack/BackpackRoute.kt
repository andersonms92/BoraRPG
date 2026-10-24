package com.borarpg.feature.backpack

import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borarpg.feature.missions.MissionViewModel

@Composable
fun BackpackRoute(missions: MissionViewModel, vm: BackpackViewModel = hiltViewModel()) {
    val hero by vm.hero.collectAsStateWithLifecycle()
    BackpackScreen(hero, missions)
}
