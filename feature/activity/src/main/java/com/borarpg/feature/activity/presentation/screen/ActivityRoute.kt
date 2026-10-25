package com.borarpg.feature.activity.presentation.screen

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borarpg.domain.model.location.GpsState
import com.borarpg.feature.activity.presentation.action.ActivityAction
import com.borarpg.feature.activity.presentation.permissions.rememberLocationPermissionState
import com.borarpg.feature.activity.presentation.state.ActivityUiState
import com.borarpg.feature.activity.presentation.viewmodel.CepViewModel
import com.borarpg.feature.activity.presentation.viewmodel.PlacesViewModel
import com.borarpg.feature.activity.presentation.viewmodel.WorkoutViewModel
import com.borarpg.feature.missions.presentation.viewmodel.MissionViewModel
import kotlinx.coroutines.flow.flowOf

@Composable
fun ActivityRoute(
    missions: MissionViewModel,
    placesViewModel: PlacesViewModel = hiltViewModel(),
    cepViewModel: CepViewModel = hiltViewModel(),
    workoutViewModel: WorkoutViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val permission = rememberLocationPermissionState()
    val workout by workoutViewModel.state.collectAsStateWithLifecycle()
    val mission by missions.state.collectAsStateWithLifecycle()
    val cep by cepViewModel.state.collectAsStateWithLifecycle()
    val savedQuests by placesViewModel.quests.collectAsStateWithLifecycle()
    val placesMessage by placesViewModel.message.collectAsStateWithLifecycle()
    val locations =
        remember(permission.granted) {
            if (permission.granted) placesViewModel.locations
            else flowOf(GpsState(message = "Ative o GPS para acompanhar sua posição."))
        }
    val gps by
        locations.collectAsStateWithLifecycle(
            initialValue = GpsState(message = "Ative o GPS para acompanhar sua posição.")
        )
    ActivityScreen(
        state =
            ActivityUiState(
                workout = workout,
                mission = mission,
                cep = cep,
                gps = gps,
                savedQuests = savedQuests,
                placesMessage = placesMessage,
                locationGranted = permission.granted,
                permissionRequested = permission.requested,
            ),
        onAction = { action ->
            when (action) {
                is ActivityAction.Workout -> workoutViewModel.onAction(action.action)
                is ActivityAction.AcceptMission -> missions.accept(action.quest)
                ActivityAction.ClaimMission -> missions.claim()
                ActivityAction.CancelMission -> missions.cancel()
                is ActivityAction.CreateQuest ->
                    placesViewModel.createQuest(action.title, action.latitude, action.longitude)
                is ActivityAction.AddQuest -> placesViewModel.addQuest(action.quest)
                is ActivityAction.DeleteQuest -> placesViewModel.deleteQuest(action.id)
                is ActivityAction.EditCep -> cepViewModel.edit(action.input)
                ActivityAction.SearchCep -> cepViewModel.search()
                ActivityAction.CancelCep -> cepViewModel.cancelSearch()
                ActivityAction.ClearCepTarget -> cepViewModel.clearTarget()
                ActivityAction.RegenerateObjectives -> cepViewModel.regenerateObjectives()
                ActivityAction.RequestLocation -> permission.request()
                ActivityAction.RefreshLocationPermission -> permission.refresh()
                ActivityAction.OpenPermissions ->
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + context.packageName),
                        )
                    )
                ActivityAction.OpenLocationSettings ->
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        },
    )
}
