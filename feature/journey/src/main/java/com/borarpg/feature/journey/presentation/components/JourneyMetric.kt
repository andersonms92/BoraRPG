package com.borarpg.feature.journey.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel

@Composable
internal fun JourneyMetric(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    horizontal: Boolean = false,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), color = Panel) {
        if (horizontal) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(22.dp))
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = Muted,
                )
                MetricValue(value, Modifier.weight(1f), TextAlign.End)
            }
        } else {
            Column(
                Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp),
                )
                MetricValue(value)
                Text(label, style = MaterialTheme.typography.labelMedium, color = Muted)
            }
        }
    }
}

@Composable
private fun MetricValue(
    value: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
) {
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val glyphUnits = value.sumOf { if (it.isDigit()) .7 else .4 }.toFloat().coerceAtLeast(1f)
        Text(
            value,
            modifier = Modifier.fillMaxWidth(),
            fontSize = minOf(22f, maxWidth.value / (glyphUnits * fontScale)).sp,
            fontWeight = FontWeight.Bold,
            color = Cream,
            maxLines = 1,
            softWrap = false,
            textAlign = textAlign,
        )
    }
}
