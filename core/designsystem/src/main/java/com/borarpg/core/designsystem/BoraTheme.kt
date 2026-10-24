package com.borarpg.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Ink = Color(0xFF101813)
val Panel = Color(0xFF1E2A22)
val Lime = Color(0xFFD5FA68)
val Muted = Color(0xFFA6B4A5)
val Coral = Color(0xFFFF9478)
val Cream = Color(0xFFF2F4E8)

@Composable
fun BoraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme =
            darkColorScheme(
                primary = Lime,
                onPrimary = Ink,
                background = Ink,
                onBackground = Cream,
                surface = Panel,
                onSurface = Cream,
                secondary = Coral,
                secondaryContainer = Color(0xFF364329),
                onSecondaryContainer = Lime,
                surfaceTint = Lime,
                outline = Color(0xFF475447),
            ),
        typography =
            Typography(
                displayLarge =
                    TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 58.sp,
                        lineHeight = 56.sp,
                        letterSpacing = (-2).sp,
                    ),
                headlineLarge =
                    TextStyle(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 36.sp,
                        lineHeight = 38.sp,
                        letterSpacing = (-1).sp,
                    ),
                headlineMedium =
                    TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 32.sp),
                titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 23.sp),
                labelSmall =
                    TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                    ),
            ),
        content = content,
    )
}

@Composable
fun Eyebrow(text: String, color: Color = Lime) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = color)
}

@Composable
fun Tag(text: String, color: Color = Lime) {
    Text(
        text,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        modifier =
            Modifier.background(color.copy(alpha = .12f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}
