package com.borarpg.domain.usecase.quest

import com.borarpg.domain.model.location.CepLocation
import com.borarpg.domain.model.mission.QuestPoint
import com.borarpg.domain.rules.mission.QuestGenerator
import kotlin.random.Random

class GenerateObjectivesUseCase(private val random: Random = Random.Default) {
    operator fun invoke(center: CepLocation): List<QuestPoint> = QuestGenerator.generate(center, random)
}
