package com.borarpg.feature.journey.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Accent
import com.borarpg.core.designsystem.theme.Coral
import com.borarpg.core.designsystem.theme.Rose
import com.borarpg.domain.model.hero.Hero
import java.text.NumberFormat

private data class MetricSpec(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val accent: Color,
)

@Composable
internal fun JourneyMetrics(hero: Hero) {
    val numbers = NumberFormat.getIntegerInstance()
    val metrics =
        listOf(
            MetricSpec(
                "Passos",
                numbers.format(hero.steps),
                Icons.AutoMirrored.Outlined.DirectionsWalk,
                Accent,
            ),
            MetricSpec("Ouro", numbers.format(hero.gold), Icons.Outlined.MonetizationOn, Coral),
            MetricSpec(
                "Vitórias",
                numbers.format(hero.victories),
                Icons.Outlined.EmojiEvents,
                Rose,
            ),
        )
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth < 320.dp || fontScale >= 1.2f) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                metrics.forEach { metric ->
                    JourneyMetric(
                        metric.label,
                        metric.value,
                        metric.icon,
                        metric.accent,
                        modifier = Modifier.fillMaxWidth(),
                        horizontal = true,
                    )
                }
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                metrics.forEach { metric ->
                    JourneyMetric(
                        metric.label,
                        metric.value,
                        metric.icon,
                        metric.accent,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
