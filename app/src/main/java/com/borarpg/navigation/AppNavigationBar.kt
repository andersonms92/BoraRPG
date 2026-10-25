package com.borarpg.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Ink
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel

@Composable
internal fun AppNavigationBar(selected: AppDestination, onSelect: (AppDestination) -> Unit) {
    Surface(color = Ink) {
        Surface(
            modifier =
                Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(22.dp),
            color = Panel,
            border = BorderStroke(1.dp, Color.White.copy(alpha = .06f)),
        ) {
            Row(
                Modifier.fillMaxWidth().selectableGroup().padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AppDestination.entries.forEach { destination ->
                    val active = selected == destination
                    val tint by
                        animateColorAsState(
                            if (active) Accent else Muted,
                            label = "Destino selecionado",
                        )
                    Column(
                        Modifier.weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (active) Accent.copy(alpha = .12f) else Color.Transparent
                            )
                            .selectable(
                                selected = active,
                                role = Role.Tab,
                                onClick = { onSelect(destination) },
                            )
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(destination.icon, null, tint = tint, modifier = Modifier.size(21.dp))
                        Text(
                            destination.title,
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = tint,
                        )
                    }
                }
            }
        }
    }
}
