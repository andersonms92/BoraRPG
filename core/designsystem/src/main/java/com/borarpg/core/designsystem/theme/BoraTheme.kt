package com.borarpg.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

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
