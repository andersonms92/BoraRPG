package com.borarpg.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BoraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme =
            darkColorScheme(
                primary = Accent,
                onPrimary = Ink,
                primaryContainer = Color(0xFF513343),
                onPrimaryContainer = Color(0xFFFFDDD1),
                background = Ink,
                onBackground = Cream,
                surface = Panel,
                onSurface = Cream,
                secondary = Rose,
                onSecondary = Ink,
                secondaryContainer = Color(0xFF39283F),
                onSecondaryContainer = Color(0xFFFFD7EA),
                tertiary = Color(0xFFB6B5EF),
                surfaceVariant = Elevated,
                onSurfaceVariant = Muted,
                surfaceTint = Accent,
                outline = Color(0xFF636A87),
                outlineVariant = Color(0xFF353A52),
                error = Color(0xFFFF9B9B),
                onError = Ink,
            ),
        typography =
            Typography(
                displayLarge =
                    TextStyle(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 46.sp,
                        lineHeight = 50.sp,
                        letterSpacing = (-1.5).sp,
                    ),
                headlineLarge =
                    TextStyle(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 34.sp,
                        lineHeight = 38.sp,
                        letterSpacing = (-1).sp,
                    ),
                headlineMedium =
                    TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 27.sp,
                        lineHeight = 32.sp,
                        letterSpacing = (-.6).sp,
                    ),
                titleLarge =
                    TextStyle(fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 27.sp),
                titleMedium =
                    TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                    ),
                bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
                bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
                bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
                labelLarge =
                    TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    ),
                labelMedium =
                    TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 17.sp),
                labelSmall =
                    TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        letterSpacing = 1.4.sp,
                    ),
            ),
        shapes =
            Shapes(
                extraSmall = RoundedCornerShape(6.dp),
                small = RoundedCornerShape(10.dp),
                medium = RoundedCornerShape(16.dp),
                large = RoundedCornerShape(22.dp),
                extraLarge = RoundedCornerShape(28.dp),
            ),
        content = content,
    )
}
