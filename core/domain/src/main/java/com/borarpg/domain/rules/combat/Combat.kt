package com.borarpg.domain.rules.combat

import com.borarpg.domain.model.hero.Hero

object Combat {
    fun advance(hero: Hero, steps: Int): Hero {
        require(steps > 0) { "Passos devem ser positivos" }
        val victories = if (steps < hero.hp) 0 else 1 + (steps - hero.hp) / 1000
        val nextHp = if (steps < hero.hp) hero.hp - steps else 1000 - (steps - hero.hp) % 1000
        return hero.copy(
            hp = nextHp,
            xp = hero.xp + (hero.steps % 10 + steps) / 10 + victories * 50,
            gold = hero.gold + victories * 25,
            victories = hero.victories + victories,
            steps = hero.steps + steps,
        )
    }
}
