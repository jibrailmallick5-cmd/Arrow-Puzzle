package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.domain.ArrowTile
import com.example.ui.theme.CobaltDark
import com.example.ui.theme.InkNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftBorderBlue
import kotlin.math.min

@Composable
fun CompletedPuzzlePreview(
    rows: Int,
    cols: Int,
    initialArrows: List<ArrowTile>,
    modifier: Modifier = Modifier
) {
    val safeRows = rows.coerceAtLeast(1)
    val safeCols = cols.coerceAtLeast(1)

    Canvas(
        modifier = modifier.aspectRatio(safeCols.toFloat() / safeRows.toFloat())
    ) {
        val pad = size.minDimension * 0.08f
        val availW = (size.width - pad * 2f).coerceAtLeast(1f)
        val availH = (size.height - pad * 2f).coerceAtLeast(1f)
        val cell = min(availW / safeCols, availH / safeRows)
        val gridW = cell * safeCols
        val gridH = cell * safeRows
        val ox = (size.width - gridW) / 2f
        val oy = (size.height - gridH) / 2f

        drawRoundRect(
            color = PureWhite,
            topLeft = Offset(0f, 0f),
            size = size,
            cornerRadius = CornerRadius(24f, 24f)
        )
        drawRoundRect(
            color = SoftBorderBlue,
            topLeft = Offset(1f, 1f),
            size = Size(size.width - 2f, size.height - 2f),
            cornerRadius = CornerRadius(24f, 24f),
            style = Stroke(width = 2f)
        )

        val lineStroke = (cell * 0.17f).coerceIn(1.8f, 6f)
        val headRadius = (cell * 0.28f).coerceIn(3f, 10f)

        for (arrow in initialArrows) {
            val color = androidx.compose.ui.graphics.Color(0xFF111318)
            val points = arrow.path.map { c ->
                Offset(ox + (c.col + 0.5f) * cell, oy + (c.row + 0.5f) * cell)
            }
            val dir = arrow.direction
            val headPt = points.last()

            val shaftPath = Path().apply {
                if (points.size == 1) {
                    moveTo(headPt.x - dir.dCol * cell * 0.22f, headPt.y - dir.dRow * cell * 0.22f)
                    lineTo(headPt.x + dir.dCol * cell * 0.06f, headPt.y + dir.dRow * cell * 0.06f)
                } else {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
            }
            drawPath(
                path = shaftPath,
                color = color,
                style = Stroke(width = lineStroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            val tip = Offset(headPt.x + dir.dCol * headRadius * 1.15f, headPt.y + dir.dRow * headRadius * 1.15f)
            val base = Offset(tip.x - dir.dCol * headRadius * 1.35f, tip.y - dir.dRow * headRadius * 1.35f)
            val px = -dir.dRow.toFloat()
            val py = dir.dCol.toFloat()
            val wing = headRadius * 0.9f
            val headPath = Path().apply {
                moveTo(tip.x, tip.y)
                lineTo(base.x + px * wing, base.y + py * wing)
                lineTo(base.x - px * wing, base.y - py * wing)
                close()
            }
            drawPath(path = headPath, color = color)
        }
    }
}
