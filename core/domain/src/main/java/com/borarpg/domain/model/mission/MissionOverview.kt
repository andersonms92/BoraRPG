package com.borarpg.domain.model.mission

import com.borarpg.domain.model.inventory.InventoryItem

data class MissionOverview(
    val active: MissionProgress?,
    val inventory: List<InventoryItem>,
    val claimed: Set<String>,
)
