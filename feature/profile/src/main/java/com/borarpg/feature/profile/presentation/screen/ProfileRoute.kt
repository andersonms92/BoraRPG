package com.borarpg.feature.profile.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borarpg.feature.profile.presentation.viewmodel.PlayerViewModel

@Composable
fun ProfileRoute(vm: PlayerViewModel = hiltViewModel()) {
    val hero by vm.hero.collectAsStateWithLifecycle()
    val state by vm.profile.collectAsStateWithLifecycle()
    ProfileScreen(state, hero, vm::edit, vm::save)
}
