package com.borarpg.domain

data class QuestPoint(
    val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val kind: String,
    val generated: Boolean = false,
    val description: String = "",
)
