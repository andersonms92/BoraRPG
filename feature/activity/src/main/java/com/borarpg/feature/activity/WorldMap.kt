package com.borarpg.feature.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borarpg.core.designsystem.*
import com.borarpg.domain.GeoLocation
import com.borarpg.domain.GpsState
import com.borarpg.domain.QuestPoint
import com.borarpg.domain.WorkoutStatus
import com.borarpg.feature.missions.MissionViewModel
import java.util.UUID
import kotlinx.coroutines.flow.flowOf
import org.maplibre.android.geometry.LatLng

@Composable
fun WorldMap(
    player: PlacesViewModel = hiltViewModel(),
    missions: MissionViewModel,
    cepViewModel: CepViewModel = hiltViewModel(),
    workoutViewModel: WorkoutViewModel = hiltViewModel(),
) {
    val workout by workoutViewModel.state.collectAsStateWithLifecycle()
    val tracking = workout.tracking
    val currentWorkout = workout.current
    val historyId = workout.historyId
    val route = workout.route
    var showDetails by rememberSaveable { mutableStateOf(false) }
    val mission by missions.state.collectAsStateWithLifecycle()
    val cep by cepViewModel.state.collectAsStateWithLifecycle()
    var showCep by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    fun granted() =
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    var permission by remember { mutableStateOf(granted()) }
    var requested by rememberSaveable { mutableStateOf(false) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permission = granted()
            requested = true
        }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) permission = granted()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val locations =
        remember(permission) {
            if (permission) player.locations
            else flowOf(GpsState(message = "Ative o GPS para acompanhar sua posição."))
        }
    val gps by
        locations.collectAsStateWithLifecycle(
            initialValue = GpsState(message = "Ative o GPS para acompanhar sua posição.")
        )
    val savedQuests by player.quests.collectAsStateWithLifecycle()
    val quests =
        (listOfNotNull(mission.active?.quest) + savedQuests + cep.objectives)
            .distinctBy { it.id }
            .filter { it.id !in mission.claimed }
    val placesMessage by player.message.collectAsStateWithLifecycle()
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
            onSelect = { selectedId = it },
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
        Column(
            Modifier.align(Alignment.TopStart)
                .onSizeChanged { topInset = it.height }
                .fillMaxWidth()
                .background(Ink.copy(alpha = .94f))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Eyebrow("O mundo é sua próxima missão")
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Atividade", style = MaterialTheme.typography.headlineLarge)
                Tag("${visible.size} LOCAIS")
            }
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Todos", "Combate", "Exploração").forEach { label ->
                    FilterChip(
                        selected = filter == label,
                        onClick = {
                            filter = label
                            selectedId = null
                        },
                        label = { Text(label) },
                    )
                }
            }
        }
        Column(
            Modifier.align(Alignment.BottomCenter)
                .onSizeChanged { bottomInset = it.height }
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalButton(onClick = { showCep = true }, shape = RoundedCornerShape(18.dp)) {
                    Icon(Icons.Outlined.Search, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Buscar CEP")
                }
                FilledIconButton(
                    onClick = {
                        cepViewModel.clearTarget()
                        follow = true
                        recenter++
                        if (!permission)
                            launcher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                )
                            )
                    },
                    modifier = Modifier.size(52.dp),
                ) {
                    Icon(Icons.Outlined.MyLocation, "Centralizar na minha posição")
                }
            }
            FilledTonalButton(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !cep.objectivesLoading,
                shape = RoundedCornerShape(18.dp),
                onClick = {
                    if (cep.target == null) showCep = true
                    else {
                        selectedId = null
                        cepViewModel.regenerateObjectives()
                    }
                },
            ) {
                Icon(Icons.Outlined.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text(if (cep.objectivesLoading) "Buscando objetivos…" else "Novos objetivos")
            }
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
                    WorkoutControls(
                        workout,
                        workoutViewModel::onAction,
                        onFollow = {
                            workoutViewModel.onAction(WorkoutAction.Follow)
                            follow = true
                            recenter++
                            permission = granted()
                        },
                        onHistory = { id ->
                            workoutViewModel.onAction(WorkoutAction.SelectHistory(id))
                            follow = false
                            recenter++
                        },
                    )
                    if (selected == null) {
                        mission.active?.let { active ->
                            Text(
                                "${active.quest.title} · ${active.steps}/${active.quest.goalSteps} passos",
                                color = Lime,
                                style = MaterialTheme.typography.labelMedium,
                            )
                            if (active.ready)
                                Button(onClick = missions::claim, enabled = !mission.busy) {
                                    Text("Resgatar recompensas")
                                }
                        }
                        TextButton(onClick = { showDetails = !showDetails }) {
                            Text(if (showDetails) "Ocultar detalhes" else "Objetivos e região")
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
                                onAccept = missions::accept,
                                onClaim = missions::claim,
                                onCancel = missions::cancel,
                                onClose = { selectedId = null },
                                onToggleSaved = { quest ->
                                    if (savedQuests.any { it.id == quest.id })
                                        player.deleteQuest(quest.id)
                                    else player.addQuest(quest)
                                },
                            )
                        } else {
                            Eyebrow(
                                if (cep.target != null) "Região por CEP · GPS não acompanha"
                                else if (permission && gps.location != null) "Localização conectada"
                                else "Explore no seu ritmo"
                            )
                            Text(
                                cep.target?.let { "CEP ${it.cep}" } ?: gps.message,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            cep.target?.let {
                                Text(
                                    it.address,
                                    color = Muted,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                if (cep.objectivesLoading) {
                                    LinearProgressIndicator(Modifier.fillMaxWidth())
                                    Text(
                                        "Procurando locais próximos…",
                                        color = Muted,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                                cep.objectivesMessage?.let { message ->
                                    Text(
                                        message,
                                        color = Muted,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                            Text(
                                "Toque nos pontos para explorar. Segure o mapa para marcar um objetivo pessoal.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Muted,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (!permission)
                                    TextButton(
                                        onClick = {
                                            if (requested)
                                                context.startActivity(
                                                    Intent(
                                                        Settings
                                                            .ACTION_APPLICATION_DETAILS_SETTINGS,
                                                        android.net.Uri.parse(
                                                            "package:${context.packageName}"
                                                        ),
                                                    )
                                                )
                                            else
                                                launcher.launch(
                                                    arrayOf(
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                                    )
                                                )
                                        }
                                    ) {
                                        Text(if (requested) "Permissões" else "Ativar GPS")
                                    }
                                else if (gps.location == null)
                                    TextButton(
                                        onClick = {
                                            context.startActivity(
                                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                            )
                                        }
                                    ) {
                                        Text("Configurar GPS")
                                    }
                            }
                            Text(
                                if (quests.isEmpty())
                                    "Nenhum objetivo salvo. Segure um local no mapa para criar o primeiro."
                                else "O percurso é gravado durante uma atividade iniciada.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Muted,
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
    if (showCep) {
        CepSearchDialog(
            cep,
            cepViewModel::edit,
            cepViewModel::search,
            {
                cepViewModel.cancelSearch()
                showCep = false
            },
        )
    }
    pending?.let { point ->
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text("Novo ponto de aventura") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Escolha um local que você conhece e pode acessar. Este objetivo fica salvo apenas no seu aparelho."
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.take(60) },
                        label = { Text("Nome do local") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = title.isNotBlank(),
                    onClick = {
                        player.addQuest(
                            QuestPoint(
                                UUID.randomUUID().toString(),
                                title,
                                point.latitude,
                                point.longitude,
                                "Exploração",
                            )
                        )
                        pending = null
                    },
                ) {
                    Text("Salvar local")
                }
            },
            dismissButton = { TextButton(onClick = { pending = null }) { Text("Cancelar") } },
        )
    }
}
