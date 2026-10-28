package com.borarpg.feature.activity.presentation.state

import com.borarpg.domain.model.mission.QuestKind

internal enum class QuestFilter(val kind: QuestKind?) {
    ALL(null),
    COMBAT(QuestKind.COMBAT),
    EXPLORATION(QuestKind.EXPLORATION),
}
