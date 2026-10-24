package com.borarpg.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.borarpg.domain.CepLocation
import com.borarpg.domain.QuestPoint
import com.borarpg.domain.RoutePoint
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

@Composable
internal fun NativeWorldMap(
    quests: List<QuestPoint>,
    location: android.location.Location?,
    follow: Boolean,
    recenter: Int,
    cepTarget: CepLocation?,
    cepRevision: Int,
    cepObjectives: List<QuestPoint>,
    onGesture: () -> Unit,
    onSelect: (String) -> Unit,
    onLongPress: (LatLng) -> Unit,
    onError: (String?) -> Unit,
    onReady: () -> Unit,
    route: List<RoutePoint> = emptyList(),
    fitRoute: Boolean = false,
    topInset: Int = 0,
    bottomInset: Int = 0,
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
    var routeSource by remember { mutableStateOf<GeoJsonSource?>(null) }
    var playerSource by remember { mutableStateOf<GeoJsonSource?>(null) }
    val view = remember {
        MapLibre.getInstance(context)
        MapView(context).apply { onCreate(null) }
    }
    DisposableEffect(view, lifecycle, density) {
        var started = false
        var resumed = false
        fun sync() {
            val shouldStart = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            val shouldResume = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            if (resumed && !shouldResume) {
                view.onPause()
                resumed = false
            }
            if (started && !shouldStart) {
                view.onStop()
                started = false
            }
            if (!started && shouldStart) {
                view.onStart()
                started = true
            }
            if (!resumed && shouldResume) {
                view.onResume()
                resumed = true
            }
        }
        val observer = LifecycleEventObserver { _, _ -> sync() }
        lifecycle.addObserver(observer)
        sync()
        val callbacks =
            object : android.content.ComponentCallbacks2 {
                override fun onConfigurationChanged(config: android.content.res.Configuration) =
                    Unit

                override fun onLowMemory() {
                    view.onLowMemory()
                }

                override fun onTrimMemory(level: Int) {
                    if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW)
                        view.onLowMemory()
                }
            }
        context.registerComponentCallbacks(callbacks)
        view.addOnDidFailLoadingMapListener {
            latestError(
                "Não foi possível carregar o mapa. Verifique sua conexão e reabra esta área."
            )
        }
        view.getMapAsync { controller ->
            map = controller
            controller.uiSettings.isCompassEnabled = false
            controller.uiSettings.attributionGravity =
                android.view.Gravity.TOP or android.view.Gravity.END
            controller.uiSettings.logoGravity =
                android.view.Gravity.TOP or android.view.Gravity.START
            controller.uiSettings.setAttributionMargins(
                0,
                (166 * density).toInt(),
                (12 * density).toInt(),
                0,
            )
            controller.uiSettings.setLogoMargins(
                (12 * density).toInt(),
                (166 * density).toInt(),
                0,
                0,
            )
            controller.moveCamera(CameraUpdateFactory.zoomTo(1.0))
            controller.addOnCameraMoveStartedListener { reason ->
                if (reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE)
                    latestGesture()
            }
            controller.addOnMapClickListener { position ->
                val p = controller.projection.toScreenLocation(position)
                val radius = 18 * density
                val feature =
                    controller
                        .queryRenderedFeatures(
                            android.graphics.RectF(
                                p.x - radius,
                                p.y - radius,
                                p.x + radius,
                                p.y + radius,
                            ),
                            "quests",
                        )
                        .firstOrNull()
                if (feature != null) {
                    latestSelect(feature.getStringProperty("id"))
                    true
                } else false
            }
            controller.addOnMapLongClickListener {
                latestLongPress(it)
                true
            }
            controller.setStyle("https://tiles.openfreemap.org/styles/dark") { style ->
                val routeData =
                    GeoJsonSource("route", FeatureCollection.fromFeatures(emptyArray<Feature>()))
                style.addSource(routeData)
                routeSource = routeData
                style.addLayer(
                    LineLayer("route-line", "route")
                        .withProperties(lineColor("#83BFFF"), lineWidth(5f))
                )
                val objectives = GeoJsonSource("objectives", questFeatures(latestQuests))
                style.addSource(objectives)
                objectivesSource = objectives
                style.addLayer(
                    CircleLayer("quests-halo", "objectives")
                        .withProperties(
                            circleRadius(19f),
                            circleColor("#D5FA68"),
                            circleOpacity(.16f),
                        )
                )
                style.addLayer(
                    CircleLayer("quests", "objectives")
                        .withProperties(
                            circleRadius(10f),
                            circleColor("#D5FA68"),
                            circleStrokeWidth(3f),
                            circleStrokeColor("#18221B"),
                        )
                )
                style.addLayer(
                    CircleLayer("combat", "objectives")
                        .withFilter(
                            Expression.eq(Expression.get("kind"), Expression.literal("Combate"))
                        )
                        .withProperties(
                            circleRadius(10f),
                            circleColor("#FF9478"),
                            circleStrokeWidth(3f),
                            circleStrokeColor("#18221B"),
                        )
                )
                style.addLayer(
                    SymbolLayer("quest-labels", "objectives")
                        .withProperties(
                            textField(Expression.get("glyph")),
                            textFont(arrayOf("Noto Sans Regular")),
                            textSize(13f),
                            textColor("#18221B"),
                            textAllowOverlap(true),
                            textIgnorePlacement(true),
                        )
                )
                val player =
                    GeoJsonSource("player", FeatureCollection.fromFeatures(emptyArray<Feature>()))
                style.addSource(player)
                playerSource = player
                style.addLayer(
                    CircleLayer("player-halo", "player")
                        .withProperties(
                            circleRadius(20f),
                            circleColor("#83BFFF"),
                            circleOpacity(.2f),
                        )
                )
                style.addLayer(
                    CircleLayer("player", "player")
                        .withProperties(
                            circleRadius(7f),
                            circleColor("#83BFFF"),
                            circleStrokeWidth(3f),
                            circleStrokeColor("#FFFFFF"),
                        )
                )
                latestError(null)
                ready = true
                latestReady()
            }
        }
        onDispose {
            lifecycle.removeObserver(observer)
            context.unregisterComponentCallbacks(callbacks)
            if (resumed) view.onPause()
            if (started) view.onStop()
            objectivesSource = null
            playerSource = null
            view.onDestroy()
            map = null
            ready = false
        }
    }
    LaunchedEffect(ready, topInset, bottomInset) {
        if (ready)
            map?.setPadding(
                0,
                topInset,
                0,
                bottomInset.coerceAtMost((view.height - topInset - 100).coerceAtLeast(0)),
            )
    }
    LaunchedEffect(objectivesSource, quests) {
        objectivesSource?.setGeoJson(questFeatures(quests))
    }
    LaunchedEffect(ready, playerSource, location, follow, recenter) {
        if (ready) {
            val points =
                location?.let {
                    listOf(Feature.fromGeometry(Point.fromLngLat(it.longitude, it.latitude)))
                } ?: emptyList()
            playerSource?.setGeoJson(FeatureCollection.fromFeatures(points))
            if (location != null && follow)
                map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude),
                        15.5,
                    )
                )
        }
    }
    LaunchedEffect(cepTarget, cepRevision, cepObjectives, ready) {
        if (ready && cepTarget != null) {
            if (cepObjectives.isNotEmpty()) {
                val points =
                    (cepObjectives.map { LatLng(it.latitude, it.longitude) } +
                            LatLng(cepTarget.latitude, cepTarget.longitude))
                        .distinct()
                if (points.size > 1) {
                    val bounds = LatLngBounds.Builder().includes(points).build()
                    map?.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(
                            bounds,
                            (24 * density).toInt().coerceAtMost(view.width / 8),
                            (if (topInset > 0) 24 * density else 170 * density)
                                .toInt()
                                .coerceAtMost(view.height / 4),
                            (24 * density).toInt().coerceAtMost(view.width / 8),
                            (if (bottomInset > 0) 24 * density else 350 * density)
                                .toInt()
                                .coerceAtMost(view.height / 2),
                        )
                    )
                    return@LaunchedEffect
                }
            }
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(cepTarget.latitude, cepTarget.longitude),
                    15.0,
                )
            )
        }
    }
    LaunchedEffect(routeSource, route, fitRoute, recenter) {
        routeSource?.setGeoJson(
            FeatureCollection.fromFeatures(
                route
                    .groupBy { it.segment }
                    .values
                    .filter { it.size > 1 }
                    .map { segment ->
                        Feature.fromGeometry(
                            LineString.fromLngLats(
                                segment.map { Point.fromLngLat(it.longitude, it.latitude) }
                            )
                        )
                    }
            )
        )
        if (ready && fitRoute && route.isNotEmpty()) {
            val coordinates = route.map { LatLng(it.latitude, it.longitude) }.distinct()
            if (coordinates.size > 1)
                map?.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        LatLngBounds.Builder().includes(coordinates).build(),
                        48,
                    )
                )
            else map?.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates.first(), 16.0))
        }
    }
    AndroidView(factory = { view }, modifier = Modifier.fillMaxSize())
}

private fun questFeatures(quests: List<QuestPoint>) =
    FeatureCollection.fromFeatures(
        quests.map { q ->
            Feature.fromGeometry(Point.fromLngLat(q.longitude, q.latitude)).apply {
                addStringProperty("id", q.id)
                addStringProperty("kind", q.kind)
                addStringProperty("glyph", if (q.kind == "Combate") "!" else "+")
            }
        }
    )
