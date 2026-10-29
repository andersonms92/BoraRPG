package com.borarpg.domain.rules.combat

import com.borarpg.domain.model.hero.Hero

object Combat {
    const val GUARDIAN_HEALTH = 5000
    const val VICTORY_XP = 50
    const val VICTORY_GOLD = 25

    fun advance(hero: Hero, steps: Int): Hero {
        require(steps > 0) { "Passos devem ser positivos" }
        val victories = if (steps < hero.hp) 0 else 1 + (steps - hero.hp) / GUARDIAN_HEALTH
        val nextHp =
            if (steps < hero.hp) hero.hp - steps
            else GUARDIAN_HEALTH - (steps - hero.hp) % GUARDIAN_HEALTH
        return hero.copy(
            hp = nextHp,
            xp = hero.xp + (hero.steps % 10 + steps) / 10 + victories * VICTORY_XP,
            gold = hero.gold + victories * VICTORY_GOLD,
            victories = hero.victories + victories,
            steps = hero.steps + steps,
        )
    }
}
