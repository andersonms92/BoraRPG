package com.borarpg.domain.model.hero

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
