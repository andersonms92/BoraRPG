package com.borarpg.feature.journey.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borarpg.core.designsystem.components.Eyebrow
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Muted

@Composable
internal fun JourneyMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Eyebrow(label, Muted)
        Text(value, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Cream)
    }
}
