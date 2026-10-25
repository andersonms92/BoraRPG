package com.borarpg.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borarpg.core.designsystem.theme.Ink
import com.borarpg.core.designsystem.theme.Lime
import com.borarpg.core.designsystem.theme.Muted
import com.borarpg.core.designsystem.theme.Panel

@Composable
internal fun AppNavigationBar(selected: AppDestination, onSelect: (AppDestination) -> Unit) {
    Surface(color = Ink, border = BorderStroke(1.dp, Panel)) {
        Row(
            Modifier.fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            AppDestination.entries.forEach { destination ->
                Column(
                    Modifier.weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onSelect(destination) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        destination.icon,
                        destination.title,
                        tint = if (selected == destination) Lime else Muted,
                        modifier = Modifier.size(23.dp),
                    )
                    Text(
                        destination.title,
                        fontSize = 11.sp,
                        fontWeight =
                            if (selected == destination) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected == destination) Lime else Muted,
                    )
                    Box(
                        Modifier.size(
                                width = if (selected == destination) 18.dp else 0.dp,
                                height = 3.dp,
                            )
                            .background(Lime, CircleShape)
                    )
                }
            }
        }
    }
}
