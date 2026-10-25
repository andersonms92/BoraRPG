package com.borarpg.feature.missions.presentation.state

import com.borarpg.domain.model.inventory.InventoryItem
import com.borarpg.domain.model.mission.MissionProgress

data class MissionState(
    val active: MissionProgress? = null,
    val inventory: List<InventoryItem> = emptyList(),
    val claimed: Set<String> = emptySet(),
    val busy: Boolean = false,
    val message: String? = null,
)
