package com.borarpg.feature.activity.presentation.map

import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.BackgroundLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.backgroundColor
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.PropertyFactory.fillOutlineColor
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.textColor
import org.maplibre.android.style.layers.PropertyFactory.textHaloColor
import org.maplibre.android.style.layers.PropertyFactory.textHaloWidth
import org.maplibre.android.style.layers.SymbolLayer

internal fun applyAdventureMapStyle(style: Style) {
    style.layers.forEach { layer ->
        val id = layer.id
        when (layer) {
            is BackgroundLayer -> layer.setProperties(backgroundColor("#14182B"))
            is FillLayer -> {
                val color =
                    when {
                        id == "water" -> "#252C47"
                        id.contains("park") || id.contains("wood") -> "#20283D"
                        id == "building" -> "#1D2238"
                        else -> "#181D30"
                    }
                layer.setProperties(fillColor(color))
                if (id == "building") layer.setProperties(fillOutlineColor("#2B304A"))
            }
            is LineLayer -> {
                val color =
                    when {
                        id.contains("water") -> "#303955"
                        id.contains("path") -> "#77748E"
                        id.contains("casing") -> "#45435E"
                        id.contains("motorway_inner") -> "#65566F"
                        id.contains("inner") || id.contains("minor") -> "#363B54"
                        else -> "#3C405A"
                    }
                layer.setProperties(lineColor(color))
            }
            is SymbolLayer ->
                layer.setProperties(
                    textColor("#AEB5CF"),
                    textHaloColor("#14182B"),
                    textHaloWidth(1.2f),
                )
        }
    }
}
