package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.domain.ArrowTile
import com.example.domain.Direction
import com.example.domain.GraphicsQuality
import com.example.domain.PathInspection
import com.example.ui.theme.AmberHint
import com.example.ui.theme.CobaltDark
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HeartCrimson
import com.example.ui.theme.IceSurface
import com.example.ui.theme.InkNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftBorderBlue
import com.example.ui.viewmodel.BlockedBumpFeedback
import com.example.ui.viewmodel.EscapingArrow
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private val SnakeSlitherEasing = CubicBezierEasing(0.32f, 0.0f, 0.25f, 1.0f)

@Composable
fun ArrowBoard3D(
    levelId: String,
    rows: Int,
    cols: Int,
    remainingArrows: List<ArrowTile>,
    escapingArrows: List<EscapingArrow>,
    hintedArrowId: Int?,
    isRulerActive: Boolean,
    allInspections: Map<Int, PathInspection>,
    blockedBump: BlockedBumpFeedback?,
    graphicsQuality: GraphicsQuality,
    onArrowTapped: (ArrowTile) -> Unit,
    modifier: Modifier = Modifier
) {
    val safeRows = rows.coerceAtLeast(1)
    val safeCols = cols.coerceAtLeast(1)
    val density = LocalDensity.current
    val maxZoom = when {
        maxOf(safeRows, safeCols) >= 22 -> 4.2f
        maxOf(safeRows, safeCols) >= 15 -> 3.4f
        else -> 2.8f
    }

    var zoom by remember(levelId) { mutableFloatStateOf(1f) }
    var panOffset by remember(levelId) { mutableStateOf(Offset.Zero) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }

    fun clampPan(raw: Offset, targetZoom: Float): Offset {
        if (targetZoom <= 1.01f || viewportSize.width == 0 || viewportSize.height == 0) {
            return Offset.Zero
        }
        val maxX = (viewportSize.width * (targetZoom - 1f)) / 2f + viewportSize.width * 0.06f
        val maxY = (viewportSize.height * (targetZoom - 1f)) / 2f + viewportSize.height * 0.06f
        return Offset(
            x = raw.x.coerceIn(-maxX, maxX),
            y = raw.y.coerceIn(-maxY, maxY)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hint_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_pulse_value"
    )

    val bumpAnim = remember { Animatable(0f) }
    LaunchedEffect(blockedBump?.eventSeq) {
        if (blockedBump != null) {
            bumpAnim.snapTo(0f)
            bumpAnim.animateTo(1f, animationSpec = tween(durationMillis = 340))
            bumpAnim.snapTo(0f)
        }
    }

    val escapeProgressMap = remember { mutableStateMapOf<Long, Float>() }
    for (esc in escapingArrows) {
        key(esc.eventSeq) {
            LaunchedEffect(esc.eventSeq) {
                val anim = Animatable(0f)
                escapeProgressMap[esc.eventSeq] = 0f
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 520, easing = SnakeSlitherEasing)
                ) {
                    escapeProgressMap[esc.eventSeq] = value
                }
                escapeProgressMap[esc.eventSeq] = 1f
            }
            DisposableEffect(esc.eventSeq) {
                onDispose {
                    escapeProgressMap.remove(esc.eventSeq)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(safeCols.toFloat() / safeRows.toFloat())
            .clip(RoundedCornerShape(24.dp))
            .clipToBounds()
            .onSizeChanged { viewportSize = it }
            .pointerInput(levelId, maxZoom) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var totalPan = Offset.Zero
                    var isPanning = false
                    val touchSlop = viewConfiguration.touchSlop
                    do {
                        val event = awaitPointerEvent()
                        val pressedCount = event.changes.count { it.pressed }
                        if (pressedCount >= 2) {
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()
                            val centroid = event.calculateCentroid(useCurrent = false)
                            val oldZoom = zoom
                            val newZoom = (oldZoom * zoomChange).coerceIn(1f, maxZoom)
                            val finalZoom = if (newZoom <= 1.02f) 1f else newZoom

                            if (finalZoom <= 1f) {
                                zoom = 1f
                                panOffset = Offset.Zero
                            } else {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val centroidOffset = centroid - center
                                val ratio = finalZoom / oldZoom
                                val rawPan = (panOffset - centroidOffset) * ratio + centroidOffset + panChange
                                zoom = finalZoom
                                panOffset = clampPan(rawPan, finalZoom)
                            }
                            isPanning = true
                            event.changes.forEach { it.consume() }
                        } else if (pressedCount == 1 && zoom > 1.02f) {
                            val panChange = event.calculatePan()
                            totalPan += panChange
                            if (isPanning || totalPan.getDistance() > touchSlop) {
                                isPanning = true
                                panOffset = clampPan(panOffset + panChange, zoom)
                                event.changes.forEach { it.consume() }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .testTag("puzzle_board"),
        contentAlignment = Alignment.Center
    ) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = zoom
                scaleY = zoom
                translationX = panOffset.x
                translationY = panOffset.y
            },
        contentAlignment = Alignment.Center
    ) {
        val boardPaddingDp = if (isRulerActive) 22.dp else 14.dp
        val boardPaddingPx = with(density) { boardPaddingDp.toPx() }
        val totalW = constraints.maxWidth.toFloat()
        val totalH = constraints.maxHeight.toFloat()

        val innerW = (totalW - boardPaddingPx * 2f).coerceAtLeast(1f)
        val innerH = (totalH - boardPaddingPx * 2f).coerceAtLeast(1f)
        val cellSize = min(innerW / safeCols, innerH / safeRows)
        val gridW = cellSize * safeCols
        val gridH = cellSize * safeRows
        val originX = (totalW - gridW) / 2f + if (isRulerActive) boardPaddingPx * 0.22f else 0f
        val originY = (totalH - gridH) / 2f + if (isRulerActive) boardPaddingPx * 0.22f else 0f

        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Crisp 3D White Board Card with subtle bevel & drop shadow
            val trayPad = cellSize * 0.32f
            val trayLeft = (originX - trayPad).coerceAtLeast(2f)
            val trayTop = (originY - trayPad).coerceAtLeast(2f)
            val trayW = (gridW + trayPad * 2f).coerceAtMost(size.width - trayLeft - 2f)
            val trayH = (gridH + trayPad * 2f).coerceAtMost(size.height - trayTop - 4f)
            val trayRadius = CornerRadius(28f, 28f)
            val trayDepth = if (graphicsQuality == GraphicsQuality.BATTERY_SAVER) 3f else 8f

            if (graphicsQuality != GraphicsQuality.BATTERY_SAVER) {
                drawRoundRect(
                    color = Color(0x1E1565C0),
                    topLeft = Offset(trayLeft + 2f, trayTop + trayDepth * 1.35f),
                    size = Size(trayW, trayH),
                    cornerRadius = trayRadius
                )
            }

            drawRoundRect(
                color = SoftBorderBlue,
                topLeft = Offset(trayLeft, trayTop + trayDepth * 0.55f),
                size = Size(trayW, trayH),
                cornerRadius = trayRadius
            )

            drawRoundRect(
                color = PureWhite,
                topLeft = Offset(trayLeft, trayTop),
                size = Size(trayW, trayH),
                cornerRadius = trayRadius
            )
            drawRoundRect(
                color = SoftBorderBlue,
                topLeft = Offset(trayLeft, trayTop),
                size = Size(trayW, trayH),
                cornerRadius = trayRadius,
                style = Stroke(width = 2.2f)
            )

            // 2. Subtle architectural micro-dots at grid centers
            val dotRadius = (cellSize * 0.045f).coerceIn(1.2f, 3.2f)
            for (r in 0 until safeRows) {
                for (c in 0 until safeCols) {
                    drawCircle(
                        color = IceSurface.copy(alpha = 0.95f).let { Color(0xFFE2ECF9) },
                        radius = dotRadius,
                        center = Offset(
                            originX + (c + 0.5f) * cellSize,
                            originY + (r + 0.5f) * cellSize
                        )
                    )
                }
            }

            // 3. Ruler Coordinates & Sightlines
            if (isRulerActive) {
                drawRulerGuides(
                    rows = safeRows,
                    cols = safeCols,
                    originX = originX,
                    originY = originY,
                    cellSize = cellSize,
                    arrows = remainingArrows,
                    inspections = allInspections
                )
            }

            // 4. Blocked Bump Impact Highlight
            if (blockedBump != null && bumpAnim.value > 0f) {
                val alpha = (1f - bumpAnim.value) * 0.7f
                for (cell in blockedBump.rayCells) {
                    drawCircle(
                        color = HeartCrimson.copy(alpha = alpha * 0.35f),
                        radius = cellSize * 0.16f,
                        center = Offset(
                            originX + (cell.col + 0.5f) * cellSize,
                            originY + (cell.row + 0.5f) * cellSize
                        )
                    )
                }
                blockedBump.blockerCell?.let { bc ->
                    val cx = originX + (bc.col + 0.5f) * cellSize
                    val cy = originY + (bc.row + 0.5f) * cellSize
                    drawCircle(
                        color = HeartCrimson.copy(alpha = alpha),
                        radius = cellSize * 0.38f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 4.2f)
                    )
                }
            }

            // 5. Slender Winding 3D Polyline Arrows (idle or bumping)
            for (arrow in remainingArrows) {
                val isBumped = blockedBump?.arrowId == arrow.id && bumpAnim.value > 0f
                val bumpAdvancePx = if (isBumped) {
                    val wave = sin(bumpAnim.value * Math.PI * 2.5).toFloat() * (1f - bumpAnim.value)
                    cellSize * 0.24f * wave
                } else {
                    0f
                }

                drawWindingArrow(
                    arrow = arrow,
                    rows = safeRows,
                    cols = safeCols,
                    originX = originX,
                    originY = originY,
                    cellSize = cellSize,
                    isHinted = arrow.id == hintedArrowId,
                    isBumped = isBumped,
                    pulse = pulse,
                    graphicsQuality = graphicsQuality,
                    alpha = 1f,
                    slitherProgress = 0f,
                    bumpAdvancePx = bumpAdvancePx
                )
            }

            // 6. Snake-Slither Escaping Arrows (follows own corners then glides out in direction)
            for (esc in escapingArrows) {
                val progress = escapeProgressMap[esc.eventSeq] ?: 0f
                val fadeAlpha = if (progress <= 0.74f) {
                    1f
                } else {
                    (1f - (progress - 0.74f) / 0.26f).coerceIn(0f, 1f)
                }

                if (fadeAlpha > 0.01f) {
                    drawWindingArrow(
                        arrow = esc.tile,
                        rows = safeRows,
                        cols = safeCols,
                        originX = originX,
                        originY = originY,
                        cellSize = cellSize,
                        isHinted = false,
                        isBumped = false,
                        pulse = 0f,
                        graphicsQuality = graphicsQuality,
                        alpha = fadeAlpha,
                        slitherProgress = progress,
                        bumpAdvancePx = 0f
                    )
                }
            }
        }

        // 7. Interactive Hit Targets on every cell of each winding arrow
        for (arrow in remainingArrows) {
            val cellDp = with(density) { cellSize.toDp() }
            arrow.path.forEachIndexed { idx, cell ->
                val isHead = idx == arrow.path.lastIndex
                val leftPx = originX + cell.col * cellSize
                val topPx = originY + cell.row * cellSize
                val baseModifier = Modifier
                    .align(Alignment.TopStart)
                    .offset { IntOffset(leftPx.roundToInt(), topPx.roundToInt()) }
                    .size(cellDp)
                    .clip(RoundedCornerShape(8.dp))
                    .let { mod ->
                        if (isHead) {
                            mod.testTag("arrow_tile_${arrow.id}")
                                .semantics {
                                    contentDescription =
                                        "Arrow pointing ${arrow.direction.name.lowercase()} at row ${arrow.headRow + 1} column ${arrow.headCol + 1}"
                                }
                        } else {
                            mod
                        }
                    }
                    .clickable(
                        interactionSource = remember(arrow.id, idx) { MutableInteractionSource() },
                        indication = ripple(bounded = false, color = CobaltPrimary.copy(alpha = 0.25f)),
                        onClick = { onArrowTapped(arrow) }
                    )
                Box(modifier = baseModifier)
            }
        }
    }
    }
}

private fun DrawScope.drawWindingArrow(
    arrow: ArrowTile,
    rows: Int,
    cols: Int,
    originX: Float,
    originY: Float,
    cellSize: Float,
    isHinted: Boolean,
    isBumped: Boolean,
    pulse: Float,
    graphicsQuality: GraphicsQuality,
    alpha: Float,
    slitherProgress: Float,
    bumpAdvancePx: Float
) {
    val lineStroke = (cellSize * 0.22f).coerceIn(2.6f, 12f)
    val headRadius = (cellSize * 0.35f).coerceIn(4.5f, 19f)

    val mainColor = when {
        isBumped -> HeartCrimson
        isHinted -> AmberHint
        else -> Color(0xFF111318)
    }.copy(alpha = alpha)

    val basePoints = arrow.path.map { cell ->
        Offset(
            x = originX + (cell.col + 0.5f) * cellSize,
            y = originY + (cell.row + 0.5f) * cellSize
        )
    }

    val headPt = basePoints.last()
    val dir = arrow.direction
    val shaftEnd = Offset(
        x = headPt.x + dir.dCol * (cellSize * 0.08f),
        y = headPt.y + dir.dRow * (cellSize * 0.08f)
    )

    // Build the idle snake body vertices from tail to shaftEnd
    val idleVertices = if (basePoints.size == 1) {
        val backPt = Offset(
            x = headPt.x - dir.dCol * (cellSize * 0.26f),
            y = headPt.y - dir.dRow * (cellSize * 0.26f)
        )
        listOf(backPt, shaftEnd)
    } else {
        val p0 = basePoints[0]
        val p1 = basePoints[1]
        val startPt = Offset(
            x = p0.x + (p1.x - p0.x) * 0.15f,
            y = p0.y + (p1.y - p0.y) * 0.15f
        )
        buildList {
            add(startPt)
            for (i in 1 until basePoints.size - 1) {
                add(basePoints[i])
            }
            add(shaftEnd)
        }
    }

    // Compute idle arc length so the snake preserves its exact body length while slithering
    val idleCum = computeCumulativeLengths(idleVertices)
    val bodyLen = idleCum.last()

    // Compute active vertices either from snake-slither progress or blocked recoil bump
    val activeVertices: List<Offset> = if (slitherProgress > 0f) {
        val distToBoardEdge = when (dir) {
            Direction.UP -> (arrow.headRow + 1.1f) * cellSize
            Direction.DOWN -> (rows - arrow.headRow + 1.1f) * cellSize
            Direction.LEFT -> (arrow.headCol + 1.1f) * cellSize
            Direction.RIGHT -> (cols - arrow.headCol + 1.1f) * cellSize
        }
        val exitTravelPx = distToBoardEdge + bodyLen + cellSize * 0.7f
        val exitPt = Offset(
            x = shaftEnd.x + dir.dCol * exitTravelPx,
            y = shaftEnd.y + dir.dRow * exitTravelPx
        )
        val fullTrack = idleVertices + exitPt
        val fullCum = computeCumulativeLengths(fullTrack)
        val headS = (bodyLen + slitherProgress * exitTravelPx).coerceAtMost(fullCum.last())
        val tailS = (headS - bodyLen).coerceAtLeast(0f)
        slicePolyline(fullTrack, fullCum, tailS, headS)
    } else if (bumpAdvancePx != 0f) {
        if (bumpAdvancePx > 0f) {
            val nudgeExitPt = Offset(
                x = shaftEnd.x + dir.dCol * (bumpAdvancePx + cellSize * 0.2f),
                y = shaftEnd.y + dir.dRow * (bumpAdvancePx + cellSize * 0.2f)
            )
            val track = idleVertices + nudgeExitPt
            val cum = computeCumulativeLengths(track)
            val headS = (bodyLen + bumpAdvancePx).coerceAtMost(cum.last())
            val tailS = (headS - bodyLen).coerceAtLeast(0f)
            slicePolyline(track, cum, tailS, headS)
        } else {
            idleVertices.map { pt ->
                Offset(pt.x + dir.dCol * bumpAdvancePx, pt.y + dir.dRow * bumpAdvancePx)
            }
        }
    } else {
        idleVertices
    }

    if (activeVertices.size < 2) return

    val activeLeadPt = activeVertices.last()
    val tipAdvance = (headRadius * 1.15f - cellSize * 0.08f).coerceAtLeast(headRadius * 0.65f)
    val tipPt = Offset(
        x = activeLeadPt.x + dir.dCol * tipAdvance,
        y = activeLeadPt.y + dir.dRow * tipAdvance
    )

    fun buildShaftPath(shift: Offset): Path = Path().apply {
        moveTo(activeVertices[0].x + shift.x, activeVertices[0].y + shift.y)
        for (i in 1 until activeVertices.size) {
            lineTo(activeVertices[i].x + shift.x, activeVertices[i].y + shift.y)
        }
    }

    fun buildHeadPath(shift: Offset): Path {
        val baseCenter = Offset(
            x = tipPt.x - dir.dCol * (headRadius * 1.35f) + shift.x,
            y = tipPt.y - dir.dRow * (headRadius * 1.35f) + shift.y
        )
        val perpX = -dir.dRow.toFloat()
        val perpY = dir.dCol.toFloat()
        val wing = headRadius * 0.92f
        return Path().apply {
            moveTo(tipPt.x + shift.x, tipPt.y + shift.y)
            lineTo(baseCenter.x + perpX * wing, baseCenter.y + perpY * wing)
            lineTo(baseCenter.x - perpX * wing, baseCenter.y - perpY * wing)
            close()
        }
    }

    // Hint radiant halo along the winding path
    if (isHinted) {
        val haloWidth = lineStroke * (2.6f + 0.9f * pulse)
        drawPath(
            path = buildShaftPath(Offset.Zero),
            color = GoldAccent.copy(alpha = (0.35f + 0.35f * pulse) * alpha),
            style = Stroke(width = haloWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawCircle(
            color = GoldAccent.copy(alpha = (0.4f + 0.35f * pulse) * alpha),
            radius = headRadius * (1.35f + 0.3f * pulse),
            center = activeLeadPt
        )
    }

    // Subtle 3D cast drop shadow underneath slender black arrow
    if (graphicsQuality != GraphicsQuality.BATTERY_SAVER) {
        val shadowShift = Offset(lineStroke * 0.42f, lineStroke * 0.58f)
        val shadowColor = Color(0x30000000).copy(alpha = 0.18f * alpha)
        drawPath(
            path = buildShaftPath(shadowShift),
            color = shadowColor,
            style = Stroke(width = lineStroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = buildHeadPath(shadowShift),
            color = shadowColor
        )
    }

    // Main crisp slender polyline shaft
    drawPath(
        path = buildShaftPath(Offset.Zero),
        color = mainColor,
        style = Stroke(width = lineStroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Sharp triangular arrowhead
    drawPath(
        path = buildHeadPath(Offset.Zero),
        color = mainColor
    )

    // Subtle specular highlight sheen on High Graphics
    if (graphicsQuality == GraphicsQuality.HIGH && !isBumped) {
        drawPath(
            path = buildShaftPath(Offset(-lineStroke * 0.14f, -lineStroke * 0.14f)),
            color = PureWhite.copy(alpha = 0.25f * alpha),
            style = Stroke(width = (lineStroke * 0.28f).coerceAtLeast(1f), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

private fun computeCumulativeLengths(points: List<Offset>): FloatArray {
    val cum = FloatArray(points.size)
    var total = 0f
    cum[0] = 0f
    for (i in 1 until points.size) {
        val dx = points[i].x - points[i - 1].x
        val dy = points[i].y - points[i - 1].y
        total += hypot(dx, dy)
        cum[i] = total
    }
    return cum
}

private fun slicePolyline(
    points: List<Offset>,
    cum: FloatArray,
    startS: Float,
    endS: Float
): List<Offset> {
    if (points.size < 2 || endS <= startS) return emptyList()
    val result = ArrayList<Offset>(points.size + 2)
    result.add(sampleAtArcLength(points, cum, startS))
    for (i in 1 until points.size - 1) {
        val s = cum[i]
        if (s > startS + 0.05f && s < endS - 0.05f) {
            result.add(points[i])
        }
    }
    result.add(sampleAtArcLength(points, cum, endS))
    return result
}

private fun sampleAtArcLength(
    points: List<Offset>,
    cum: FloatArray,
    targetS: Float
): Offset {
    if (targetS <= 0f) return points.first()
    val total = cum.last()
    if (targetS >= total) return points.last()
    for (i in 1 until points.size) {
        if (cum[i] >= targetS) {
            val segLen = (cum[i] - cum[i - 1]).coerceAtLeast(0.0001f)
            val t = ((targetS - cum[i - 1]) / segLen).coerceIn(0f, 1f)
            val a = points[i - 1]
            val b = points[i]
            return Offset(
                x = a.x + (b.x - a.x) * t,
                y = a.y + (b.y - a.y) * t
            )
        }
    }
    return points.last()
}

private fun DrawScope.drawRulerGuides(
    rows: Int,
    cols: Int,
    originX: Float,
    originY: Float,
    cellSize: Float,
    arrows: List<ArrowTile>,
    inspections: Map<Int, PathInspection>
) {
    val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.rgb(21, 101, 192)
        textSize = (cellSize * 0.36f).coerceIn(18f, 32f)
        textAlign = android.graphics.Paint.Align.CENTER
        isFakeBoldText = true
    }

    for (c in 0 until cols) {
        val cx = originX + (c + 0.5f) * cellSize
        val label = ('A'.code + c).toChar().toString()
        drawContext.canvas.nativeCanvas.drawText(label, cx, originY - cellSize * 0.22f, textPaint)
    }
    for (r in 0 until rows) {
        val cy = originY + (r + 0.6f) * cellSize
        drawContext.canvas.nativeCanvas.drawText("${r + 1}", originX - cellSize * 0.34f, cy, textPaint)
    }

    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)

    for (arrow in arrows) {
        val inspection = inspections[arrow.id] ?: continue
        val startX = originX + (arrow.headCol + 0.5f) * cellSize
        val startY = originY + (arrow.headRow + 0.5f) * cellSize

        val endPoint = if (inspection.isClear) {
            val steps = when (arrow.direction) {
                Direction.UP -> arrow.headRow + 0.6f
                Direction.DOWN -> (rows - 1 - arrow.headRow) + 0.6f
                Direction.LEFT -> arrow.headCol + 0.6f
                Direction.RIGHT -> (cols - 1 - arrow.headCol) + 0.6f
            }
            Offset(
                x = startX + arrow.direction.dCol * steps * cellSize,
                y = startY + arrow.direction.dRow * steps * cellSize
            )
        } else {
            val bc = inspection.blockerCell ?: continue
            Offset(
                x = originX + (bc.col + 0.5f) * cellSize,
                y = originY + (bc.row + 0.5f) * cellSize
            )
        }

        val lineColor = if (inspection.isClear) {
            EmeraldSuccess.copy(alpha = 0.85f)
        } else {
            HeartCrimson.copy(alpha = 0.45f)
        }

        drawLine(
            color = lineColor,
            start = Offset(startX, startY),
            end = endPoint,
            strokeWidth = if (inspection.isClear) 3.8f else 2.4f,
            cap = StrokeCap.Round,
            pathEffect = dashEffect
        )

        if (inspection.isClear) {
            drawCircle(
                color = EmeraldSuccess,
                radius = (cellSize * 0.1f).coerceIn(3f, 7f),
                center = endPoint
            )
        } else {
            val cross = (cellSize * 0.14f).coerceIn(3f, 8f)
            drawLine(
                color = HeartCrimson.copy(alpha = 0.78f),
                start = Offset(endPoint.x - cross, endPoint.y - cross),
                end = Offset(endPoint.x + cross, endPoint.y + cross),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = HeartCrimson.copy(alpha = 0.78f),
                start = Offset(endPoint.x + cross, endPoint.y - cross),
                end = Offset(endPoint.x - cross, endPoint.y + cross),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
}
