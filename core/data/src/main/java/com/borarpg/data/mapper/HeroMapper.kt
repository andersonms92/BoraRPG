package com.borarpg.data.mapper

import com.borarpg.data.local.entity.HeroEntity
import com.borarpg.domain.model.hero.Hero

internal fun HeroEntity.toDomain() = Hero(xp, gold, hp, victories, steps)

internal fun Hero.toEntity() =
    HeroEntity(xp = xp, gold = gold, hp = hp, victories = victories, steps = steps)
