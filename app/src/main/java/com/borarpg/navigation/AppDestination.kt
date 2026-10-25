package com.borarpg.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Backpack
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

internal enum class AppDestination(val title: String, val icon: ImageVector) {
    JOURNEY("Jornada", Icons.Outlined.Explore),
    ACTIVITY("Atividade", Icons.AutoMirrored.Outlined.DirectionsRun),
    BACKPACK("Mochila", Icons.Outlined.Backpack),
    PROFILE("Você", Icons.Outlined.Person),
}
