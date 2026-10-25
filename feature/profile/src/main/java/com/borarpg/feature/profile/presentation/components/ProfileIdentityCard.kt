package com.borarpg.feature.profile.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Coral
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Ink
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel
import com.borarpg.core.designsystem.theme.Rose
import com.borarpg.domain.model.hero.Hero

@Composable
internal fun ProfileIdentityCard(name: String, hero: Hero) {
    val initials =
        name
            .trim()
            .split(' ')
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Panel,
        border = BorderStroke(1.dp, Cream.copy(alpha = .06f)),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    Modifier.size(68.dp)
                        .background(
                            Brush.linearGradient(listOf(Coral, Rose)),
                            RoundedCornerShape(23.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (initials.isBlank())
                        Icon(
                            Icons.Outlined.PersonOutline,
                            null,
                            tint = Ink,
                            modifier = Modifier.size(32.dp),
                        )
                    else
                        Text(
                            initials,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                        )
                }
                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        name.trim().ifBlank { "Seu explorador" },
                        style = MaterialTheme.typography.titleLarge,
                        color = Cream,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "Sua jornada, no seu ritmo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
            }
            HorizontalDivider(color = Cream.copy(alpha = .07f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IdentityMetric("NÍVEL", hero.level.toString())
                IdentityMetric("XP TOTAL", hero.xp.toString())
                IdentityMetric("OURO", hero.gold.toString())
            }
        }
    }
}

@Composable
private fun IdentityMetric(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Muted)
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Accent,
        )
    }
}
