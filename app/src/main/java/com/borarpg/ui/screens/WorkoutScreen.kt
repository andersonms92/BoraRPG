package com.borarpg.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.borarpg.data.*
import com.borarpg.domain.*
import com.borarpg.ui.*
import com.borarpg.ui.components.MissionProgressCard
import com.borarpg.ui.map.NativeWorldMap
import com.borarpg.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun WorkoutScreen(
    player: PlayerViewModel,
    missions: MissionViewModel,
    cep: CepViewModel,
    vm: WorkoutViewModel = viewModel(),
) {
    val context = LocalContext.current
    val workouts by vm.workouts.collectAsStateWithLifecycle()
    val tracking by vm.tracking.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val savedQuests by player.quests.collectAsStateWithLifecycle()
    val mission by missions.state.collectAsStateWithLifecycle()
    val territory by cep.state.collectAsStateWithLifecycle()
    var mode by rememberSaveable { mutableStateOf(WorkoutType.WALK) }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var follow by remember { mutableStateOf(true) }
    var recenter by remember { mutableIntStateOf(0) }
    var cancelConfirmation by remember { mutableStateOf(false) }
    var selectedQuest by remember { mutableStateOf<QuestPoint?>(null) }
    val current = workouts.firstOrNull { it.status != "saved" }
    val displayed = current ?: workouts.firstOrNull { it.id == selected } ?: workouts.firstOrNull()
    val points = remember(displayed?.routeJson) { displayed?.points() ?: emptyList() }
    val quests =
        (listOfNotNull(mission.active?.quest) + savedQuests + territory.objectives)
            .distinctBy { it.id }
            .filter { it.id !in mission.claimed }
    val position =
        if (current?.status == "active") tracking.location
        else
            points.lastOrNull()?.let { point ->
                Location("history").apply {
                    latitude = point.latitude
                    longitude = point.longitude
                }
            }
    fun startOrResume() {
        val manager = context.getSystemService(LocationManager::class.java)
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            error = "Ative o GPS do aparelho para registrar o percurso."
            return
        }
        if (
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            error = "Autorize a localização precisa para registrar seu percurso."
            return
        }
        error = null
        follow = true
        if (current?.status == "paused") vm.resume() else vm.start(mode)
    }
    val permission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            startOrResume()
        }
    fun requestStart() {
        val permissions =
            mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        if (Build.VERSION.SDK_INT >= 29) permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        if (Build.VERSION.SDK_INT >= 33) permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        permission.launch(permissions.toTypedArray())
    }
    Column(
        Modifier.fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            current?.mode?.label ?: "Sua próxima atividade",
            style = MaterialTheme.typography.titleLarge,
        )
        if (current == null)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WorkoutType.entries.forEach { type ->
                    FilterChip(
                        selected = (current?.mode ?: mode) == type,
                        onClick = { mode = type },
                        label = { Text(type.label) },
                    )
                }
            }
        val elapsed =
            if (current?.status == "active")
                maxOf(displayed?.elapsedMillis ?: 0, tracking.elapsedMillis)
            else displayed?.elapsedMillis ?: 0
        val distance = displayed?.distanceMeters ?: 0.0
        val kcal = displayed?.let { workoutCalories(it.mode, it.weightKg, elapsed, distance) }
        Surface(color = Panel, shape = MaterialTheme.shapes.large) {
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    formatActivityTime(elapsed),
                    style = MaterialTheme.typography.displaySmall,
                    color = Lime,
                )
                Text(
                    String.format(
                        Locale.getDefault(),
                        "%.2f km  ·  %s kcal estimadas",
                        distance / 1000,
                        kcal?.let { "%.0f".format(it) } ?: "—",
                    )
                )
                if (kcal != null && distance > 0)
                    Text(
                        String.format(
                            Locale.getDefault(),
                            "%.3f kcal/m · %.0f kcal/km",
                            kcal / distance,
                            kcal / distance * 1000,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                Text(
                    when (current?.status) {
                        "active" ->
                            if (points.isEmpty()) "Aguardando GPS preciso"
                            else "GPS ativo · ${points.size} pontos registrados"
                        "paused" -> "Atividade pausada"
                        "draft" -> "Finalizada · salve o resultado"
                        else ->
                            if (displayed != null) "Percurso salvo · ${displayed.mode.label}"
                            else "Escolha a modalidade e comece"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(240.dp)) {
            NativeWorldMap(
                quests = quests,
                location = position,
                follow = follow && current != null,
                recenter = recenter,
                cepTarget = territory.target.takeIf { position == null },
                cepRevision = territory.revision,
                cepObjectives = quests,
                onGesture = { follow = false },
                onSelect = { id -> selectedQuest = quests.find { it.id == id } },
                onLongPress = {},
                onError = { error = it },
                onReady = {},
                route = points,
                fitRoute = current == null || current.status == "draft",
            )
            mission.active?.let { active ->
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp),
                    color = Panel,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(
                        "${active.quest.title} · ${active.steps}/${active.quest.goalSteps} passos",
                        modifier = Modifier.padding(10.dp),
                        color = Lime,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
        TextButton(
            onClick = {
                follow = true
                recenter++
            }
        ) {
            Text("Centralizar percurso / GPS")
        }
        when (current?.status) {
            "active" ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = vm::pause) { Text("Pausar") }
                    OutlinedButton(onClick = vm::finish) { Text("Finalizar") }
                }
            "paused" ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { requestStart() }) { Text("Retomar") }
                    OutlinedButton(onClick = vm::finish) { Text("Finalizar") }
                }
            "draft" ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.save(current) }) { Text("Salvar resultado") }
                    OutlinedButton(onClick = { vm.discard(current) }) { Text("Descartar") }
                }
            else ->
                Button(onClick = { requestStart() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Iniciar ${mode.label.lowercase()}")
                }
        }
        if (current?.status == "active" || current?.status == "paused")
            TextButton(onClick = { cancelConfirmation = true }) {
                Text("Cancelar atividade", color = Coral)
            }
        (error ?: tracking.error ?: message)?.let { Text(it, color = Coral) }
        mission.active?.let {
            MissionProgressCard(it, mission.busy, missions::claim, missions::cancel)
        }
        if (quests.isEmpty())
            Text(
                "Gere objetivos pelo CEP na aba Território. Eles também aparecerão neste mapa.",
                color = Muted,
            )
        if (!vm.sensorAvailable)
            Text(
                "Sem sensor de passos: o GPS registra o percurso, mas missões por passos precisam de um aparelho compatível.",
                color = Muted,
            )
        mission.message?.let { Text(it, color = Lime) }
        if (displayed != null && displayed.weightKg == null)
            Text(
                "Informe seu peso no perfil para estimar kcal nas próximas atividades.",
                color = Muted,
            )
        Text("Seus percursos", style = MaterialTheme.typography.titleLarge)
        if (workouts.none { it.status == "saved" })
            Text("As atividades salvas aparecerão aqui.", color = Muted)
        workouts
            .filter { it.status == "saved" }
            .forEach { result ->
                OutlinedButton(
                    onClick = {
                        selected = result.id
                        follow = false
                        recenter++
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "${SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(result.startedAt))} · ${result.mode.label} · ${"%.2f".format(result.distanceMeters / 1000)} km · ${formatActivityTime(result.elapsedMillis)}"
                    )
                }
            }
        Text(
            "Estimativa por peso, tempo ativo e modalidade (MET). O gasto real varia com intensidade e terreno.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
    }
    if (cancelConfirmation)
        AlertDialog(
            onDismissRequest = { cancelConfirmation = false },
            title = { Text("Cancelar atividade?") },
            text = {
                Text(
                    "O percurso e o resultado desta atividade serão descartados. Os passos já registrados na jornada permanecem."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        cancelConfirmation = false
                        if (tracking.active) vm.cancel() else current?.let(vm::discard)
                    }
                ) {
                    Text("Cancelar atividade")
                }
            },
            dismissButton = {
                TextButton(onClick = { cancelConfirmation = false }) { Text("Continuar") }
            },
        )
    selectedQuest?.let { quest ->
        AlertDialog(
            onDismissRequest = { selectedQuest = null },
            title = { Text(quest.title) },
            text = {
                Text(
                    "${quest.goalSteps} passos · ${quest.xpReward} XP · ${quest.goldReward} ouro\n${quest.itemReward}\nEncontro virtual: avance com passos após aceitar. Não é necessário entrar no local do marcador."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = mission.active == null && quest.goalSteps > 0,
                    onClick = {
                        missions.accept(quest)
                        selectedQuest = null
                    },
                ) {
                    Text("Aceitar missão")
                }
            },
            dismissButton = { TextButton(onClick = { selectedQuest = null }) { Text("Fechar") } },
        )
    }
}
