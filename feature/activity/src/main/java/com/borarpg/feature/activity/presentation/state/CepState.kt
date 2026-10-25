package com.borarpg.feature.activity.presentation.state

import com.borarpg.domain.model.location.CepLocation
import com.borarpg.domain.model.mission.QuestPoint

data class CepState(
    val input: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val target: CepLocation? = null,
    val revision: Int = 0,
    val objectives: List<QuestPoint> = emptyList(),
    val objectivesMessage: String? = null,
)
