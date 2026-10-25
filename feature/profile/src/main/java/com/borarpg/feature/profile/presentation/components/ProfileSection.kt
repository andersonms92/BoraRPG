package com.borarpg.feature.profile.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.unit.dp
import com.borarpg.core.designsystem.theme.Cream
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel

@Composable
internal fun ProfileSection(
    title: String,
    description: String,
    icon: ImageVector,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Panel,
        border = BorderStroke(1.dp, Cream.copy(alpha = .05f)),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier.size(42.dp)
                        .background(accent.copy(alpha = .12f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, null, tint = accent, modifier = Modifier.size(22.dp))
                }
                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = Cream)
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
            }
            content()
        }
    }
}
