package com.borarpg.feature.profile

import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ProfileRoute(vm: PlayerViewModel = hiltViewModel()) {
    val hero by vm.hero.collectAsStateWithLifecycle()
    ProfileScreen(vm, hero)
}
