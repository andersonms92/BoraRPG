package com.borarpg.feature.journey.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borarpg.core.designsystem.illustration.GuardianArt
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Elevated
import com.borarpg.core.designsystem.theme.Ink
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel

@Composable
internal fun ExplorationCard(onMap: () -> Unit, onActivity: () -> Unit) {
    val fontScale = LocalDensity.current.fontScale
    Surface(shape = RoundedCornerShape(28.dp), color = Panel) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val compact = maxWidth < 340.dp || fontScale >= 1.2f
            Box(
                Modifier.matchParentSize().background(Brush.linearGradient(listOf(Panel, Elevated)))
            )
            if (!compact) {
                GuardianArt(
                    Modifier.align(Alignment.CenterEnd)
                        .size(238.dp)
                        .offset(x = 61.dp, y = 5.dp)
                        .alpha(.9f)
                )
            }
            Column(
                Modifier.fillMaxWidth().padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "SUA AVENTURA CONTINUA",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = Accent,
                        fontWeight = FontWeight.Bold,
                    )
                    if (compact) GuardianArt(Modifier.size(96.dp))
                }
                Text(
                    "Cada passo\nmuda o jogo.",
                    fontSize = 33.sp,
                    lineHeight = 37.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cream,
                )
                Text(
                    "Encontre missões.\nEvolua no seu ritmo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
                Button(
                    onClick = onMap,
                    shape = RoundedCornerShape(14.dp),
                    colors =
                        ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Ink),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("Explorar mapa", fontWeight = FontWeight.Bold)
                        Icon(Icons.Outlined.NorthEast, null, modifier = Modifier.size(18.dp))
                    }
                }
                TextButton(onClick = onActivity) {
                    Text("Caminhar ou correr", color = Cream)
                }
            }
        }
    }
}
