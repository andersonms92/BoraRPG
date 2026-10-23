package com.borarpg.domain

data class Hero(
    val xp: Int = 0,
    val gold: Int = 0,
    val hp: Int = 1000,
    val victories: Int = 0,
    val steps: Int = 0,
) {
    val level: Int
        get() = xp / 100 + 1
}

object Combat {
    fun advance(hero: Hero, steps: Int): Hero {
        require(steps in 1..500) { "Passos devem estar entre 1 e 500" }
        val defeated = steps >= hero.hp
        return hero.copy(
            hp = if (defeated) 1000 else hero.hp - steps,
            xp = hero.xp + steps / 10 + if (defeated) 50 else 0,
            gold = hero.gold + if (defeated) 25 else 0,
            victories = hero.victories + if (defeated) 1 else 0,
            steps = hero.steps + steps,
        )
    }
}
