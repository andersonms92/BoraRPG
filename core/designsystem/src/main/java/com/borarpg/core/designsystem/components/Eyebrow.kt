package com.borarpg.core.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.borarpg.core.designsystem.theme.Accent

@Composable
fun Eyebrow(text: String, color: Color = Accent) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = color)
}
