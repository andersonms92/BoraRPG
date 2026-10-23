package com.borarpg.ui.map

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.borarpg.data.GpsState
import com.borarpg.data.LocationRepository
import com.borarpg.domain.QuestPoint
import com.borarpg.ui.CepViewModel
import com.borarpg.ui.PlayerViewModel
import com.borarpg.ui.theme.*
import java.util.UUID
import kotlinx.coroutines.flow.flowOf
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.*
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.geojson.*

@Composable
fun WorldMap(player: PlayerViewModel) {
    val cepViewModel: CepViewModel = viewModel()
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
            if (permission) LocationRepository(context.applicationContext).updates()
            else flowOf(GpsState(message = "Ative o GPS para acompanhar sua posição."))
        }
    val gps by
        locations.collectAsStateWithLifecycle(
            initialValue = GpsState(message = "Ative o GPS para acompanhar sua posição.")
        )
    val quests by player.quests.collectAsStateWithLifecycle()
    val profile by player.profile.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf("Todos") }
    val visible = quests.filter { filter == "Todos" || it.kind == filter }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = visible.firstOrNull { it.id == selectedId }
    var pending by remember { mutableStateOf<LatLng?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var follow by rememberSaveable { mutableStateOf(true) }
    var recenter by remember { mutableIntStateOf(0) }
    var mapError by remember { mutableStateOf<String?>(null) }
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
            gps.location,
            follow,
            recenter,
            cep.target,
            cep.revision,
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
        )
        if (loading) CircularProgressIndicator(Modifier.align(Alignment.Center), color = Lime)
        Column(
            Modifier.align(Alignment.TopStart)
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
                Text("Território", style = MaterialTheme.typography.headlineLarge)
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
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Ink.copy(alpha = .97f),
                tonalElevation = 6.dp,
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (selected != null) {
                        Eyebrow("Seu objetivo de exploração", Coral)
                        Text(selected.title, style = MaterialTheme.typography.titleLarge)
                        val reference =
                            cep.target?.let { LatLng(it.latitude, it.longitude) }
                                ?: gps.location?.let { LatLng(it.latitude, it.longitude) }
                        val distance = reference?.let { loc ->
                            FloatArray(1)
                                .also {
                                    android.location.Location.distanceBetween(
                                        loc.latitude,
                                        loc.longitude,
                                        selected.latitude,
                                        selected.longitude,
                                        it,
                                    )
                                }[0]
                        }
                        Text(
                            if (distance == null)
                                "${selected.kind} · ative o GPS para ver a distância"
                            else
                                "${selected.kind} · ${if (distance < 1000) "${distance.toInt()} m" else "%.1f km".format(distance / 1000)} em linha reta${if (cep.target != null) " a partir do CEP" else " pelo GPS"}",
                            color = Muted,
                        )
                        Text(
                            "Local marcado por você. Visite apenas áreas de acesso público.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                        Row {
                            TextButton(onClick = { selectedId = null }) { Text("Fechar") }
                            TextButton(
                                onClick = {
                                    player.deleteQuest(selected.id)
                                    selectedId = null
                                }
                            ) {
                                Text("Remover local")
                            }
                        }
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
                                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
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
                            else "GPS somente com o mapa aberto.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                    mapError?.let {
                        Text(it, color = Coral, style = MaterialTheme.typography.bodySmall)
                    }
                    profile.message
                        ?.takeIf { it.contains("local", true) }
                        ?.let { Text(it, color = Coral) }
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
