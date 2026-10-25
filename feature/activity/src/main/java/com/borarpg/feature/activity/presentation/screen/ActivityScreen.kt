package com.borarpg.feature.activity.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Coral
import com.borarpg.core.designsystem.theme.Ink
import com.borarpg.core.designsystem.theme.Lime
import com.borarpg.domain.model.location.GeoLocation
import com.borarpg.domain.model.workout.WorkoutStatus
import com.borarpg.feature.activity.presentation.action.ActivityAction
import com.borarpg.feature.activity.presentation.action.WorkoutAction
import com.borarpg.feature.activity.presentation.components.ActivityHeader
import com.borarpg.feature.activity.presentation.components.ActivityPanelButton
import com.borarpg.feature.activity.presentation.components.CepSearchDialog
import com.borarpg.feature.activity.presentation.components.CreateQuestDialog
import com.borarpg.feature.activity.presentation.components.LocationDetails
import com.borarpg.feature.activity.presentation.components.MapToolbar
import com.borarpg.feature.activity.presentation.components.ObjectiveDetails
import com.borarpg.feature.activity.presentation.components.WorkoutPanel
import com.borarpg.feature.activity.presentation.map.NativeWorldMap
import com.borarpg.feature.activity.presentation.map.toMapLocation
import com.borarpg.feature.activity.presentation.permissions.WorkoutControls
import com.borarpg.feature.activity.presentation.state.ActivityUiState
import org.maplibre.android.geometry.LatLng

@Composable
internal fun ActivityScreen(state: ActivityUiState, onAction: (ActivityAction) -> Unit) {
    val workout = state.workout
    val tracking = workout.tracking
    val currentWorkout = workout.current
    val historyId = workout.historyId
    val route = workout.route
    val mission = state.mission
    val cep = state.cep
    val gps = state.gps
    val savedQuests = state.savedQuests
    val placesMessage = state.placesMessage
    val quests = state.quests
    val permission = state.locationGranted
    val requested = state.permissionRequested
    var panelExpanded by rememberSaveable { mutableStateOf(false) }
    var showDetails by rememberSaveable { mutableStateOf(false) }
    var showCep by rememberSaveable { mutableStateOf(false) }
    var filter by rememberSaveable { mutableStateOf("Todos") }
    val visible = quests.filter { filter == "Todos" || it.kind == filter }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = visible.firstOrNull { it.id == selectedId }
    var pending by remember { mutableStateOf<LatLng?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var follow by rememberSaveable { mutableStateOf(true) }
    var recenter by remember { mutableIntStateOf(0) }
    var mapError by remember { mutableStateOf<String?>(null) }
    var topInset by remember { mutableIntStateOf(0) }
    var bottomInset by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(cep.revision) {
        if (cep.target != null) {
            follow = false
            selectedId = null
            showCep = false
        }
    }
    Box(Modifier.fillMaxSize()) {
        NativeWorldMap(
            visible,
            if (currentWorkout?.status == WorkoutStatus.ACTIVE)
                tracking.location?.toMapLocation() ?: gps.location?.toMapLocation()
            else if (historyId != null || currentWorkout != null)
                route.lastOrNull()?.let { point ->
                    android.location.Location("route").apply {
                        latitude = point.latitude
                        longitude = point.longitude
                    }
                } ?: gps.location?.toMapLocation()
            else gps.location?.toMapLocation(),
            follow,
            recenter,
            cep.target.takeIf { historyId == null && currentWorkout == null },
            cep.revision,
            cep.objectives,
            onGesture = { follow = false },
            onSelect = {
                selectedId = it
                panelExpanded = true
            },
            onLongPress = {
                pending = it
                title = ""
            },
            onError = {
                mapError = it
                if (it != null) loading = false
            },
            onReady = { loading = false },
            topInset = topInset,
            bottomInset = bottomInset,
            route = route,
            fitRoute =
                historyId != null && currentWorkout == null ||
                    currentWorkout?.status == WorkoutStatus.DRAFT,
        )
        if (loading) CircularProgressIndicator(Modifier.align(Alignment.Center), color = Lime)
        ActivityHeader(
            objectivesCount = visible.size,
            filter = filter,
            onFilter = {
                filter = it
                selectedId = null
            },
            modifier = Modifier.align(Alignment.TopStart).onSizeChanged { topInset = it.height },
        )
        WorkoutControls(
            state = workout,
            onAction = { onAction(ActivityAction.Workout(it)) },
            onFollow = {
                onAction(ActivityAction.Workout(WorkoutAction.Follow))
                follow = true
                recenter++
                onAction(ActivityAction.RefreshLocationPermission)
            },
            onHistory = { id ->
                onAction(ActivityAction.Workout(WorkoutAction.SelectHistory(id)))
                follow = false
                recenter++
            },
        ) { panelState, requestStart ->
            Column(
                Modifier.align(Alignment.BottomCenter)
                    .onSizeChanged { bottomInset = it.height }
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MapToolbar(
                    onSearchCep = { showCep = true },
                    onRecenter = {
                        onAction(ActivityAction.ClearCepTarget)
                        follow = true
                        recenter++
                        if (!permission) onAction(ActivityAction.RequestLocation)
                    },
                    onNewObjectives = {
                        if (cep.target == null) showCep = true
                        else {
                            selectedId = null
                            onAction(ActivityAction.RegenerateObjectives)
                        }
                    },
                    activityButton = {
                        ActivityPanelButton(
                            expanded = panelExpanded,
                            status = panelState.current?.status,
                            mode = panelState.current?.mode ?: panelState.mode,
                            hasError = panelState.message != null || mapError != null,
                            onClick = { panelExpanded = !panelExpanded },
                        )
                    },
                )
                AnimatedVisibility(
                    visible = panelExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = Ink.copy(alpha = .97f),
                        tonalElevation = 6.dp,
                    ) {
                        Column(
                            Modifier.heightIn(max = 340.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            WorkoutPanel(
                                state = panelState,
                                onAction = { onAction(ActivityAction.Workout(it)) },
                                requestStart = requestStart,
                            )
                            if (selected == null) {
                                mission.active?.let { active ->
                                    Text(
                                        "${active.quest.title} · ${active.steps}/${active.quest.goalSteps} passos",
                                        color = Lime,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                    if (active.ready)
                                        Button(
                                            onClick = { onAction(ActivityAction.ClaimMission) },
                                            enabled = !mission.busy,
                                        ) {
                                            Text("Resgatar recompensas")
                                        }
                                }
                                TextButton(onClick = { showDetails = !showDetails }) {
                                    Text(
                                        if (showDetails) "Ocultar detalhes"
                                        else "Objetivos e região"
                                    )
                                }
                            }
                            if (selected != null || showDetails) {
                                if (selected != null) {
                                    ObjectiveDetails(
                                        selected = selected,
                                        mission = mission,
                                        reference =
                                            cep.target?.let {
                                                GeoLocation(it.latitude, it.longitude)
                                            } ?: gps.location,
                                        fromCep = cep.target != null,
                                        saved = savedQuests.any { it.id == selected.id },
                                        onAccept = { onAction(ActivityAction.AcceptMission(it)) },
                                        onClaim = { onAction(ActivityAction.ClaimMission) },
                                        onCancel = { onAction(ActivityAction.CancelMission) },
                                        onClose = { selectedId = null },
                                        onToggleSaved = { quest ->
                                            if (savedQuests.any { it.id == quest.id })
                                                onAction(ActivityAction.DeleteQuest(quest.id))
                                            else onAction(ActivityAction.AddQuest(quest))
                                        },
                                    )
                                } else {
                                    LocationDetails(
                                        cep = cep,
                                        gps = gps,
                                        locationGranted = permission,
                                        permissionRequested = requested,
                                        hasQuests = quests.isNotEmpty(),
                                        onPermission = {
                                            onAction(
                                                if (requested) ActivityAction.OpenPermissions
                                                else ActivityAction.RequestLocation
                                            )
                                        },
                                        onLocationSettings = {
                                            onAction(ActivityAction.OpenLocationSettings)
                                        },
                                    )
                                }
                            }
                            mapError?.let {
                                Text(it, color = Coral, style = MaterialTheme.typography.bodySmall)
                            }
                            placesMessage
                                ?.takeIf { it.contains("local", true) }
                                ?.let { Text(it, color = Coral) }
                            mission.message?.let { Text(it, color = Lime) }
                        }
                    }
                }
            }
        }
    }
    if (showCep) {
        CepSearchDialog(
            cep,
            { onAction(ActivityAction.EditCep(it)) },
            { onAction(ActivityAction.SearchCep) },
            {
                onAction(ActivityAction.CancelCep)
                showCep = false
            },
        )
    }
    pending?.let { point ->
        CreateQuestDialog(
            title = title,
            onTitleChange = { title = it },
            onConfirm = {
                onAction(ActivityAction.CreateQuest(title, point.latitude, point.longitude))
                pending = null
            },
            onDismiss = { pending = null },
        )
    }
}
