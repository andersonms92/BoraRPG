package com.borarpg.data.mapper

import com.borarpg.data.local.entity.ItemEntity
import com.borarpg.data.local.entity.MissionEntity
import com.borarpg.domain.model.inventory.InventoryItem
import com.borarpg.domain.model.mission.QuestPoint

internal fun MissionEntity.toDomain() =
    QuestPoint(
        id,
        title,
        latitude,
        longitude,
        kind,
        true,
        description,
        goalSteps,
        xpReward,
        goldReward,
        itemReward,
        rarity,
    )

internal fun QuestPoint.toEntity(baselineSteps: Int) =
    MissionEntity(
        id,
        title,
        latitude,
        longitude,
        kind,
        description,
        goalSteps,
        xpReward,
        goldReward,
        itemReward,
        rarity,
        baselineSteps,
    )

internal fun ItemEntity.toDomain() = InventoryItem(name, quantity)
