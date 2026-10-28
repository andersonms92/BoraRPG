package com.borarpg.feature.missions.presentation.formatter

import com.borarpg.domain.model.mission.QuestKind
import com.borarpg.domain.model.mission.QuestRarity

val QuestKind.label: String
    get() =
        when (this) {
            QuestKind.COMBAT -> "Combate"
            QuestKind.EXPLORATION -> "Exploração"
        }

val QuestRarity.label: String
    get() =
        when (this) {
            QuestRarity.COMMON -> "Comum"
            QuestRarity.RARE -> "Raro"
            QuestRarity.EPIC -> "Épico"
        }
