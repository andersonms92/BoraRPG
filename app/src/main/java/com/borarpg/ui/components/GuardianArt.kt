package com.borarpg.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.borarpg.ui.theme.Lime

@Composable
fun GuardianArt(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawCircle(Lime.copy(alpha = .08f), w * .45f, Offset(w * .62f, h * .5f))
        repeat(6) { i ->
            drawCircle(
                Lime.copy(alpha = .12f),
                w * (.18f + i * .075f),
                Offset(w * .62f, h * .52f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()),
            )
        }
        fun polygon(color: Color, vararg points: Pair<Float, Float>) {
            val path =
                Path().apply {
                    moveTo(points[0].first * w, points[0].second * h)
                    points.drop(1).forEach { lineTo(it.first * w, it.second * h) }
                    close()
                }
            drawPath(path, color)
        }
        polygon(
            Color(0xFF344D37),
            .24f to .97f,
            .36f to .67f,
            .58f to .55f,
            .84f to .69f,
            .97f to .97f,
        )
        polygon(
            Color(0xFF72925B),
            .4f to .28f,
            .73f to .22f,
            .85f to .5f,
            .72f to .76f,
            .43f to .71f,
            .32f to .47f,
        )
        polygon(
            Color(0xFF98B976),
            .4f to .28f,
            .6f to .36f,
            .57f to .72f,
            .43f to .71f,
            .32f to .47f,
        )
        polygon(Color(0xFF263B2C), .36f to .41f, .53f to .43f, .5f to .51f, .4f to .5f)
        polygon(Color(0xFF263B2C), .62f to .43f, .8f to .37f, .77f to .49f, .65f to .51f)
        drawLine(Lime, Offset(w * .4f, h * .46f), Offset(w * .49f, h * .47f), 3.dp.toPx())
        drawLine(Lime, Offset(w * .66f, h * .47f), Offset(w * .75f, h * .44f), 3.dp.toPx())
        polygon(Color(0xFF38533A), .48f to .58f, .69f to .57f, .62f to .64f, .52f to .64f)
        polygon(
            Color(0xFF91AC62),
            .43f to .3f,
            .34f to .12f,
            .27f to .08f,
            .35f to .1f,
            .42f to .19f,
            .49f to .27f,
        )
        polygon(
            Color(0xFF91AC62),
            .68f to .26f,
            .77f to .07f,
            .88f to .04f,
            .81f to .11f,
            .77f to .3f,
        )
        polygon(Lime, .31f to .18f, .19f to .18f, .23f to .12f)
        polygon(Lime, .8f to .14f, .89f to .16f, .88f to .1f)
        polygon(Lime, .56f to .8f, .62f to .87f, .57f to .94f, .51f to .87f)
        listOf(.17f to .53f, .91f to .36f, .27f to .77f).forEach {
            drawCircle(Lime, 2.dp.toPx(), Offset(it.first * w, it.second * h))
        }
    }
}
