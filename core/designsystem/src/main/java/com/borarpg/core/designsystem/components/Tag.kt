package com.borarpg.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Accent

@Composable
fun Tag(text: String, color: Color = Accent) {
    Text(
        text,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        modifier =
            Modifier.background(color.copy(alpha = .12f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}
