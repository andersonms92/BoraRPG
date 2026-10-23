package com.borarpg.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borarpg.data.*
import com.borarpg.domain.QuestPoint
import kotlinx.coroutines.flow.flowOf
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.*
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.*
import java.util.UUID

@Composable fun WorldMap(player: PlayerViewModel) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    fun granted() = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    var permission by remember { mutableStateOf(granted()) }
    var requested by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission = granted(); requested = true }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) permission = granted() }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val locations = remember(permission) { if (permission) LocationRepository(context.applicationContext).updates() else flowOf(GpsState(message = "Ative o GPS para acompanhar sua posição.")) }
    val gps by locations.collectAsStateWithLifecycle(initialValue = GpsState(message = "Ative o GPS para acompanhar sua posição."))
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
    var showDemo by remember { mutableIntStateOf(0) }
    var mapError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    Box(Modifier.fillMaxSize()) {
        NativeWorldMap(visible, gps.location, follow, recenter, showDemo,
            onGesture = { follow = false }, onSelect = { selectedId = it },
            onLongPress = { pending = it; title = "" }, onError = { mapError = it; if (it != null) loading = false }, onReady = { loading = false })
        if (loading) CircularProgressIndicator(Modifier.align(Alignment.Center), color = Lime)
        Column(Modifier.align(Alignment.TopStart).fillMaxWidth().background(Ink.copy(alpha = .94f)).statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Eyebrow("O mundo é sua próxima missão")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Território", style = MaterialTheme.typography.headlineLarge)
                Tag("${visible.size} LOCAIS")
            }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Todos", "Combate", "Exploração").forEach { label -> FilterChip(selected = filter == label, onClick = { filter = label; selectedId = null }, label = { Text(label) }) }
            }
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                FilledIconButton(onClick = {
                    if (permission) { follow = true; recenter++ }
                    else launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }, modifier = Modifier.size(52.dp)) { Icon(Icons.Outlined.MyLocation, "Centralizar na minha posição") }
            }
            Surface(shape = RoundedCornerShape(26.dp), color = Ink.copy(alpha = .97f), tonalElevation = 6.dp) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (selected != null) {
                        Eyebrow(if (selected.demo) "Objetivo de demonstração · Ibirapuera" else "Seu objetivo de exploração", Coral)
                        Text(selected.title, style = MaterialTheme.typography.titleLarge)
                        val distance = gps.location?.let { loc -> FloatArray(1).also { android.location.Location.distanceBetween(loc.latitude, loc.longitude, selected.latitude, selected.longitude, it) }[0] }
                        Text(if (distance == null) "${selected.kind} · ative o GPS para ver a distância" else "${selected.kind} · ${if (distance < 1000) "${distance.toInt()} m" else "%.1f km".format(distance / 1000)} em linha reta", color = Muted)
                        Text(if (selected.demo) "Encontro ilustrativo. Ainda não concede recompensas." else "Local marcado por você. Visite apenas áreas de acesso público.", style = MaterialTheme.typography.bodySmall, color = Muted)
                        Row {
                            TextButton(onClick = { selectedId = null }) { Text("Fechar") }
                            if (!selected.demo) TextButton(onClick = { player.deleteQuest(selected.id); selectedId = null }) { Text("Remover local") }
                        }
                    } else {
                        Eyebrow(if (permission && gps.location != null) "Localização conectada" else "Explore no seu ritmo")
                        Text(gps.message, style = MaterialTheme.typography.titleMedium)
                        Text("Toque nos pontos para explorar. Segure o mapa para marcar um objetivo pessoal.", style = MaterialTheme.typography.bodySmall, color = Muted)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (!permission) TextButton(onClick = {
                                if (requested) context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:${context.packageName}")))
                                else launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                            }) { Text(if (requested) "Permissões" else "Ativar GPS") }
                            else if (gps.location == null) TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }) { Text("Configurar GPS") }
                            TextButton(onClick = { follow = false; showDemo++ }) { Text("Ver exemplos") }
                        }
                        Text("Exemplos no Ibirapuera, SP · GPS somente com o mapa aberto.", style = MaterialTheme.typography.labelSmall, color = Muted)
                    }
                    mapError?.let { Text(it, color = Coral, style = MaterialTheme.typography.bodySmall) }
                    profile.message?.takeIf { it.contains("local", true) }?.let { Text(it, color = Coral) }
                }
            }
        }
    }
    pending?.let { point ->
        AlertDialog(onDismissRequest = { pending = null }, title = { Text("Novo ponto de aventura") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Escolha um local que você conhece e pode acessar. Este objetivo fica salvo apenas no seu aparelho.")
                OutlinedTextField(value = title, onValueChange = { title = it.take(60) }, label = { Text("Nome do local") }, singleLine = true)
            }
        }, confirmButton = { TextButton(enabled = title.isNotBlank(), onClick = {
            player.addQuest(QuestPoint(UUID.randomUUID().toString(), title, point.latitude, point.longitude, "Exploração")); pending = null
        }) { Text("Salvar local") } }, dismissButton = { TextButton(onClick = { pending = null }) { Text("Cancelar") } })
    }
}

@Composable private fun NativeWorldMap(
    quests: List<QuestPoint>, location: android.location.Location?, follow: Boolean, recenter: Int, showDemo: Int,
    onGesture: () -> Unit, onSelect: (String) -> Unit, onLongPress: (LatLng) -> Unit, onError: (String?) -> Unit, onReady: () -> Unit
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val density = LocalDensity.current.density
    val latestGesture by rememberUpdatedState(onGesture)
    val latestSelect by rememberUpdatedState(onSelect)
    val latestLongPress by rememberUpdatedState(onLongPress)
    val latestError by rememberUpdatedState(onError)
    val latestReady by rememberUpdatedState(onReady)
    val latestQuests by rememberUpdatedState(quests)
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var ready by remember { mutableStateOf(false) }
    var objectivesSource by remember { mutableStateOf<GeoJsonSource?>(null) }
    var playerSource by remember { mutableStateOf<GeoJsonSource?>(null) }
    val view = remember {
        MapLibre.getInstance(context)
        MapView(context).apply { onCreate(null) }
    }
    DisposableEffect(view, lifecycle, density) {
        var started = false; var resumed = false
        fun sync() {
            val shouldStart = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            val shouldResume = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            if (resumed && !shouldResume) { view.onPause(); resumed = false }
            if (started && !shouldStart) { view.onStop(); started = false }
            if (!started && shouldStart) { view.onStart(); started = true }
            if (!resumed && shouldResume) { view.onResume(); resumed = true }
        }
        val observer = LifecycleEventObserver { _, _ -> sync() }
        lifecycle.addObserver(observer); sync()
        val callbacks = object : android.content.ComponentCallbacks2 {
            override fun onConfigurationChanged(config: android.content.res.Configuration) = Unit
            override fun onLowMemory() { view.onLowMemory() }
            override fun onTrimMemory(level: Int) { if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) view.onLowMemory() }
        }
        context.registerComponentCallbacks(callbacks)
        view.addOnDidFailLoadingMapListener { latestError("Não foi possível carregar o mapa. Verifique sua conexão e reabra esta área.") }
        view.getMapAsync { controller ->
            map = controller
            controller.uiSettings.isCompassEnabled = false
            controller.uiSettings.attributionGravity = android.view.Gravity.TOP or android.view.Gravity.END
            controller.uiSettings.logoGravity = android.view.Gravity.TOP or android.view.Gravity.START
            controller.uiSettings.setAttributionMargins(0, (166 * density).toInt(), (12 * density).toInt(), 0)
            controller.uiSettings.setLogoMargins((12 * density).toInt(), (166 * density).toInt(), 0, 0)
            controller.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-23.5874, -46.6576), 14.5))
            controller.addOnCameraMoveStartedListener { reason -> if (reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) latestGesture() }
            controller.addOnMapClickListener { position ->
                val p = controller.projection.toScreenLocation(position)
                val radius = 18 * density
                val feature = controller.queryRenderedFeatures(android.graphics.RectF(p.x - radius, p.y - radius, p.x + radius, p.y + radius), "quests").firstOrNull()
                if (feature != null) { latestSelect(feature.getStringProperty("id")); true } else false
            }
            controller.addOnMapLongClickListener { latestLongPress(it); true }
            controller.setStyle("https://tiles.openfreemap.org/styles/dark") { style ->
                val objectives = GeoJsonSource("objectives", questFeatures(latestQuests))
                style.addSource(objectives)
                objectivesSource = objectives
                style.addLayer(CircleLayer("quests-halo", "objectives").withProperties(circleRadius(19f), circleColor("#D5FA68"), circleOpacity(.16f)))
                style.addLayer(CircleLayer("quests", "objectives").withProperties(circleRadius(10f), circleColor("#D5FA68"), circleStrokeWidth(3f), circleStrokeColor("#18221B")))
                style.addLayer(CircleLayer("combat", "objectives").withFilter(Expression.eq(Expression.get("kind"), Expression.literal("Combate"))).withProperties(circleRadius(10f), circleColor("#FF9478"), circleStrokeWidth(3f), circleStrokeColor("#18221B")))
                style.addLayer(SymbolLayer("quest-labels", "objectives").withProperties(textField(Expression.get("glyph")), textFont(arrayOf("Noto Sans Regular")), textSize(13f), textColor("#18221B"), textAllowOverlap(true), textIgnorePlacement(true)))
                val player = GeoJsonSource("player", FeatureCollection.fromFeatures(emptyArray<Feature>()))
                style.addSource(player)
                playerSource = player
                style.addLayer(CircleLayer("player-halo", "player").withProperties(circleRadius(20f), circleColor("#83BFFF"), circleOpacity(.2f)))
                style.addLayer(CircleLayer("player", "player").withProperties(circleRadius(7f), circleColor("#83BFFF"), circleStrokeWidth(3f), circleStrokeColor("#FFFFFF")))
                latestError(null); ready = true; latestReady()
            }
        }
        onDispose {
            lifecycle.removeObserver(observer); context.unregisterComponentCallbacks(callbacks)
            if (resumed) view.onPause()
            if (started) view.onStop()
            objectivesSource = null; playerSource = null
            view.onDestroy(); map = null; ready = false
        }
    }
    LaunchedEffect(objectivesSource, quests) {
        objectivesSource?.setGeoJson(questFeatures(quests))
    }
    LaunchedEffect(ready, playerSource, location, follow, recenter) {
        if (ready) {
            val points = location?.let { listOf(Feature.fromGeometry(Point.fromLngLat(it.longitude, it.latitude))) } ?: emptyList()
            playerSource?.setGeoJson(FeatureCollection.fromFeatures(points))
            if (location != null && follow) map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15.5))
        }
    }
    LaunchedEffect(showDemo, ready) { if (showDemo > 0 && ready) map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-23.5874, -46.6576), 14.5)) }
    AndroidView(factory = { view }, modifier = Modifier.fillMaxSize())
}

private fun questFeatures(quests: List<QuestPoint>) = FeatureCollection.fromFeatures(quests.map { q ->
    Feature.fromGeometry(Point.fromLngLat(q.longitude, q.latitude)).apply {
        addStringProperty("id", q.id)
        addStringProperty("kind", q.kind)
        addStringProperty("glyph", if (q.kind == "Combate") "!" else "+")
    }
})
