package com.borarpg.domain

data class QuestPoint(
    val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val kind: String,
    val demo: Boolean = false,
)

val sampleQuests =
    listOf(
        QuestPoint("demo-1", "Guardião do lago", -23.5874, -46.6576, "Combate", true),
        QuestPoint("demo-2", "Bosque dos fragmentos", -23.5850, -46.6601, "Exploração", true),
        QuestPoint("demo-3", "Portal da alvorada", -23.5901, -46.6559, "Exploração", true),
    )
