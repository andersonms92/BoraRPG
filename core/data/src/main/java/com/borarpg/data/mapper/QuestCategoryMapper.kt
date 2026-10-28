package com.borarpg.data.mapper

import com.borarpg.domain.model.mission.QuestKind
import com.borarpg.domain.model.mission.QuestRarity
import java.util.Locale

internal fun String.toQuestKind(): QuestKind =
    when (trim().lowercase(Locale.ROOT)) {
        "combat", "combate" -> QuestKind.COMBAT
        "exploration", "exploração", "exploracao" -> QuestKind.EXPLORATION
        else -> QuestKind.EXPLORATION
    }

internal fun QuestKind.toStorageValue(): String =
    when (this) {
        QuestKind.COMBAT -> "combat"
        QuestKind.EXPLORATION -> "exploration"
    }

internal fun String.toQuestRarity(): QuestRarity =
    when (trim().lowercase(Locale.ROOT)) {
        "common", "comum" -> QuestRarity.COMMON
        "rare", "raro" -> QuestRarity.RARE
        "epic", "épico", "epico" -> QuestRarity.EPIC
        else -> QuestRarity.COMMON
    }

internal fun QuestRarity.toStorageValue(): String =
    when (this) {
        QuestRarity.COMMON -> "common"
        QuestRarity.RARE -> "rare"
        QuestRarity.EPIC -> "epic"
    }
