package com.borarpg.domain.rules.mission

import com.borarpg.domain.model.location.CepLocation
import com.borarpg.domain.model.mission.QuestKind
import com.borarpg.domain.model.mission.QuestPoint
import com.borarpg.domain.model.mission.QuestRarity
import java.util.UUID
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object QuestGenerator {
    const val RADIUS_METERS = 1000
    const val MAX_OBJECTIVES = 6

    fun generate(center: CepLocation, random: Random = Random.Default): List<QuestPoint> {
        require(center.latitude in -90.0..90.0 && center.longitude in -180.0..180.0)
        val offset = random.nextDouble(0.0, 2 * PI)
        return (0 until MAX_OBJECTIVES).map { index ->
            val rarity =
                when (index) {
                    5 -> QuestRarity.EPIC
                    3,
                    4 -> QuestRarity.RARE
                    else -> QuestRarity.COMMON
                }
            val kind = if (index % 2 == 0) QuestKind.COMBAT else QuestKind.EXPLORATION
            val title =
                when (index) {
                    0 -> "Batedor das sombras"
                    1 -> "Baú do viajante"
                    2 -> "Guardião de pedra"
                    3 -> "Ruínas esquecidas"
                    4 -> "Sentinela ancestral"
                    else -> "Tesouro lendário"
                }
            val bearing = offset + index * PI / 3 + random.nextDouble(-.15, .15)
            val distance = random.nextDouble(150.0, RADIUS_METERS.toDouble()) / 6371000
            val lat = Math.toRadians(center.latitude)
            val lon = Math.toRadians(center.longitude)
            val targetLat = asin(sin(lat) * cos(distance) + cos(lat) * sin(distance) * cos(bearing))
            val targetLon =
                lon +
                    atan2(
                        sin(bearing) * sin(distance) * cos(lat),
                        cos(distance) - sin(lat) * sin(targetLat),
                    )
            QuestPoint(
                id = "quest:${UUID.randomUUID()}",
                title = title,
                latitude = Math.toDegrees(targetLat),
                longitude = (Math.toDegrees(targetLon) + 540) % 360 - 180,
                kind = kind,
                generated = true,
                description =
                    if (kind == QuestKind.COMBAT) "Derrote o inimigo avançando com seus passos."
                    else "Avance com seus passos para abrir o tesouro.",
                goalSteps =
                    when (rarity) {
                        QuestRarity.EPIC -> random.nextInt(1600, 2401)
                        QuestRarity.RARE -> random.nextInt(900, 1401)
                        QuestRarity.COMMON -> random.nextInt(400, 801)
                    },
                xpReward =
                    when (rarity) {
                        QuestRarity.EPIC -> random.nextInt(300, 451)
                        QuestRarity.RARE -> random.nextInt(150, 221)
                        QuestRarity.COMMON -> random.nextInt(60, 91)
                    },
                goldReward =
                    when (rarity) {
                        QuestRarity.EPIC -> random.nextInt(120, 181)
                        QuestRarity.RARE -> random.nextInt(60, 91)
                        QuestRarity.COMMON -> random.nextInt(20, 41)
                    },
                itemReward =
                    when (rarity) {
                        QuestRarity.EPIC -> "Cristal ancestral"
                        QuestRarity.RARE -> "Relíquia de prata"
                        QuestRarity.COMMON -> "Fragmento de bronze"
                    },
                rarity = rarity,
            )
        }
    }
}
