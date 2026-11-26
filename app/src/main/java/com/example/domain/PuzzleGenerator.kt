package com.example.domain

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

object PuzzleGenerator {

    private enum class BoardShape(val displayName: String) {
        CLASSIC("Classic"),
        HEART("Love Heart"),
        FLOWER("Flower Blossom"),
        TRIANGLE("Triangle"),
        LEAF("Botanical Leaf"),
        BUTTERFLY("Butterfly"),
        STAR("Starburst"),
        LOTUS("Lotus Flower"),
        CLOVER("Four-Leaf Clover"),
        DIAMOND("Gem Diamond"),
        CROWN("Royal Crown"),
        HEXAGON("Honeycomb"),
        SHIELD("Knight Shield"),
        PINE("Evergreen"),
        PINWHEEL("Pinwheel"),
        OCTAGON("Octagon Crest"),
        CASTLE("Citadel")
    }

    private data class LevelSpec(
        val rows: Int,
        val cols: Int,
        val targetArrows: Int,
        val minPathLen: Int,
        val maxPathLen: Int,
        val maxBends: Int,
        val blockingAffinity: Int,
        val shape: BoardShape,
        val rotationVariant: Int,
        val edgeVariant: Int,
        val patternTag: String
    )

    fun getTutorialPuzzle(): PuzzleLevel {
        val arrows = listOf(
            ArrowTile(
                id = 1,
                path = listOf(
                    GridCell(4, 2),
                    GridCell(3, 2),
                    GridCell(3, 3)
                ),
                direction = Direction.RIGHT,
                toneIndex = 0
            ),
            ArrowTile(
                id = 2,
                path = listOf(
                    GridCell(4, 0),
                    GridCell(4, 1),
                    GridCell(3, 1)
                ),
                direction = Direction.UP,
                toneIndex = 1
            ),
            ArrowTile(
                id = 3,
                path = listOf(
                    GridCell(0, 3),
                    GridCell(0, 2),
                    GridCell(0, 1),
                    GridCell(1, 1)
                ),
                direction = Direction.LEFT,
                toneIndex = 2
            )
        )
        return PuzzleLevel(
            id = "tutorial",
            levelNumber = 0,
            title = "Tutorial",
            rows = 5,
            cols = 5,
            arrows = arrows,
            difficulty = DifficultyTier.VERY_EASY,
            mode = GameMode.TUTORIAL,
            patternTag = "Guided"
        )
    }

    fun generateCampaignLevel(levelNumber: Int): PuzzleLevel {
        val safeLevel = levelNumber.coerceAtLeast(1)
        if (safeLevel == 1) {
            return createBeginnerLevel1Tutorial()
        }
        val tier = DifficultyTier.forLevel(safeLevel)
        val spec = specForCampaignLevel(safeLevel, tier)
        val seed = 0x5EED_CAFE_0000L xor (safeLevel.toLong() * 2654435761L)
        val arrows = generateGuaranteedSolvableWindingArrows(
            rows = spec.rows,
            cols = spec.cols,
            targetCount = spec.targetArrows,
            minPathLen = spec.minPathLen,
            maxPathLen = spec.maxPathLen,
            maxBends = spec.maxBends,
            blockingAffinity = spec.blockingAffinity,
            shape = spec.shape,
            rotationVariant = spec.rotationVariant,
            edgeVariant = spec.edgeVariant,
            seed = seed
        )
        return PuzzleLevel(
            id = "campaign_$safeLevel",
            levelNumber = safeLevel,
            title = "Level $safeLevel",
            rows = spec.rows,
            cols = spec.cols,
            arrows = arrows,
            difficulty = tier,
            mode = GameMode.CAMPAIGN,
            patternTag = spec.patternTag
        )
    }

    private fun createBeginnerLevel1Tutorial(): PuzzleLevel {
        // Simple, crystal-clear Level 1 starter tutorial:
        // Arrow 1 slides RIGHT freely; Arrow 2 is blocked by Arrow 1 until Arrow 1 escapes UP.
        val starterArrows = listOf(
            ArrowTile(
                id = 1,
                path = listOf(GridCell(1, 1), GridCell(1, 2)),
                direction = Direction.RIGHT,
                toneIndex = 0
            ),
            ArrowTile(
                id = 2,
                path = listOf(GridCell(3, 2), GridCell(2, 2)),
                direction = Direction.UP,
                toneIndex = 1
            )
        )
        return PuzzleLevel(
            id = "campaign_1",
            levelNumber = 1,
            title = "Level 1",
            rows = 5,
            cols = 5,
            arrows = starterArrows,
            difficulty = DifficultyTier.VERY_EASY,
            mode = GameMode.CAMPAIGN,
            patternTag = "Beginner Tutorial"
        )
    }

    fun generateDailyLevel(epochDay: Long, dayOfMonth: Int): PuzzleLevel {
        val seed = 0xDA11_7000_0000L xor (epochDay * 1103515245L + 12345L)
        val shapes = BoardShape.entries.filter { it != BoardShape.CLASSIC }
        val shape = shapes[(dayOfMonth * 3 + (epochDay % shapes.size).toInt()) % shapes.size]
        val tier = when (dayOfMonth % 4) {
            0 -> DifficultyTier.HARD
            1 -> DifficultyTier.MEDIUM
            2 -> DifficultyTier.EASY_MEDIUM
            else -> DifficultyTier.VERY_HARD
        }
        val dim = 15 + (dayOfMonth % 5)
        val arrows = generateGuaranteedSolvableWindingArrows(
            rows = dim,
            cols = dim,
            targetCount = 24 + (dayOfMonth % 18),
            minPathLen = 3,
            maxPathLen = 10,
            maxBends = 5,
            blockingAffinity = 12,
            shape = shape,
            rotationVariant = (dayOfMonth % 4),
            edgeVariant = (dayOfMonth % 4),
            seed = seed
        )
        return PuzzleLevel(
            id = "daily_$epochDay",
            levelNumber = dayOfMonth,
            title = "Daily #$dayOfMonth",
            rows = dim,
            cols = dim,
            arrows = arrows,
            difficulty = tier,
            mode = GameMode.DAILY_CHALLENGE,
            patternTag = shape.displayName,
            dailyEpochDay = epochDay
        )
    }

    fun generateTournamentStage(eventId: Int, stageIndex: Int): PuzzleLevel {
        val safeStage = stageIndex.coerceIn(1, 5)
        val seed = 0x700B_A3E1_0000L xor (eventId.toLong() * 999983L + safeStage * 7919L)
        val dim = 12 + safeStage * 3 // 15x15 up to 27x27
        val targetArrows = 12 + safeStage * 10 // 22 up to 62
        val shape = when (safeStage) {
            1 -> BoardShape.HEART
            2 -> BoardShape.FLOWER
            3 -> BoardShape.TRIANGLE
            4 -> BoardShape.STAR
            else -> BoardShape.LOTUS
        }
        val tier = when (safeStage) {
            1 -> DifficultyTier.EASY_MEDIUM
            2 -> DifficultyTier.MEDIUM
            3 -> DifficultyTier.HARD
            4 -> DifficultyTier.VERY_HARD
            else -> DifficultyTier.ADVANCED
        }
        val arrows = generateGuaranteedSolvableWindingArrows(
            rows = dim,
            cols = dim,
            targetCount = targetArrows,
            minPathLen = 3,
            maxPathLen = 7 + safeStage * 2,
            maxBends = 3 + safeStage,
            blockingAffinity = 10 + safeStage * 3,
            shape = shape,
            rotationVariant = (eventId + safeStage) % 4,
            edgeVariant = safeStage % 4,
            seed = seed
        )
        return PuzzleLevel(
            id = "tournament_${eventId}_$safeStage",
            levelNumber = safeStage,
            title = "Stage $safeStage / 5",
            rows = dim,
            cols = dim,
            arrows = arrows,
            difficulty = tier,
            mode = GameMode.TOURNAMENT,
            patternTag = shape.displayName,
            tournamentStage = safeStage
        )
    }

    private fun specForCampaignLevel(level: Int, tier: DifficultyTier): LevelSpec {
        val shapedPalette = listOf(
            BoardShape.FLOWER,
            BoardShape.HEART,
            BoardShape.TRIANGLE,
            BoardShape.LEAF,
            BoardShape.LOTUS,
            BoardShape.BUTTERFLY,
            BoardShape.STAR,
            BoardShape.CLOVER,
            BoardShape.DIAMOND,
            BoardShape.CROWN,
            BoardShape.PINWHEEL,
            BoardShape.SHIELD,
            BoardShape.PINE,
            BoardShape.HEXAGON,
            BoardShape.OCTAGON,
            BoardShape.CASTLE
        )

        val shape = when {
            level <= 9 -> BoardShape.CLASSIC
            else -> shapedPalette[(level - 10) % shapedPalette.size]
        }

        val rotationVariant = when (shape) {
            BoardShape.CLASSIC, BoardShape.HEART, BoardShape.CROWN, BoardShape.PINE, BoardShape.CASTLE -> 0
            BoardShape.TRIANGLE -> if (level <= 30) 0 else ((level - 10) / shapedPalette.size) % 4
            else -> ((level - 10) / shapedPalette.size + level * 3) % 4
        }
        val edgeVariant = (level * 5 + level / 3) % 4
        val patternTag = shape.displayName

        return when (tier) {
            // 1. Beginner Levels (Level 1-5): 2-4 short, simple arrows
            DifficultyTier.VERY_EASY -> {
                val count = when (level) {
                    1 -> 2
                    2 -> 3
                    3 -> 3
                    4 -> 4
                    else -> 4
                }
                val dim = if (level <= 2) 5 else 6
                val maxLen = if (level <= 2) 3 else 4
                val maxBends = if (level <= 2) 0 else 1
                val affinity = 2 + level
                LevelSpec(dim, dim, count, 2, maxLen, maxBends, affinity, shape, rotationVariant, edgeVariant, patternTag)
            }

            // 2. Easy Levels (Level 6-9: 5-9 arrows; Level 10+: noticeably more arrows 14-24)
            DifficultyTier.EASY_MEDIUM -> {
                if (level <= 9) {
                    val count = 5 + (level - 6) * 4 / 3 // 5, 6, 7, 9
                    val dim = if (level <= 7) 7 else 8
                    val maxBends = 2
                    val affinity = 5 + (level - 6)
                    LevelSpec(dim, dim, count, 2, 4, maxBends, affinity, shape, rotationVariant, edgeVariant, patternTag)
                } else {
                    val count = 14 + (level - 10) * 2 // 14, 16, 18, 20, 22, 24
                    val dim = 13 + (level - 10) / 2 // 13x13 up to 15x15
                    val maxBends = 3
                    val affinity = 9 + (level - 10)
                    LevelSpec(dim, dim, count, 3, 6, maxBends, affinity, shape, rotationVariant, edgeVariant, patternTag)
                }
            }

            // 3. Medium Levels (Level 16-30): 26-50 arrows, richer shapes & longer paths
            DifficultyTier.MEDIUM -> {
                val count = 26 + ((level - 16) * 24) / 14 // 26 smoothly up to 50
                val dim = 17 + ((level - 16) * 5) / 14 // 17x17 up to 22x22
                val maxLen = 7 + ((level - 16) * 3) / 14 // 7 up to 10
                val maxBends = 4 + ((level - 16) / 7) // 4 up to 6
                val affinity = 12 + ((level - 16) * 5) / 14
                LevelSpec(dim, dim, count, 3, maxLen, maxBends, affinity, shape, rotationVariant, edgeVariant, patternTag)
            }

            // 4. Hard Levels (Level 31-50): 52-90 arrows, long arrows & challenging blocking
            DifficultyTier.HARD -> {
                val count = 52 + ((level - 31) * 38) / 19 // 52 smoothly up to 90
                val dim = 23 + ((level - 31) * 6) / 19 // 23x23 up to 29x29
                val maxLen = 9 + ((level - 31) * 4) / 19 // 9 up to 13
                val maxBends = 6 + ((level - 31) / 10) // 6 up to 8
                val affinity = 17 + ((level - 31) * 6) / 19
                LevelSpec(dim, dim, count, 3, maxLen, maxBends, affinity, shape, rotationVariant, edgeVariant, patternTag)
            }

            // 5. Expert Levels (Level 51-100): 92-155 arrows, large boards & multi-step lookahead
            DifficultyTier.VERY_HARD -> {
                val count = 92 + ((level - 51) * 63) / 49 // 92 smoothly up to 155
                val dim = 30 + ((level - 51) * 8) / 49 // 30x30 up to 38x38
                val maxLen = 12 + ((level - 51) * 4) / 49 // 12 up to 16
                val maxBends = 9
                val affinity = 22 + ((level - 51) * 7) / 49
                LevelSpec(dim, dim, count, 3, maxLen, maxBends, affinity, shape, rotationVariant, edgeVariant, patternTag)
            }

            // 6. Master Levels (Level 101+): smooth continuous climb above 155 arrows
            DifficultyTier.ADVANCED -> {
                val extra = (level - 100).coerceAtLeast(1)
                val count = (155 + (extra + 5) / 6).coerceAtMost(240)
                val dim = (38 + extra / 100).coerceAtMost(46)
                val minLen = if (level >= 250) 4 else 3
                val maxLen = (16 + extra / 250).coerceAtMost(19)
                val affinity = (29 + extra / 150).coerceAtMost(34)
                LevelSpec(dim, dim, count, minLen, maxLen, 10, affinity, shape, rotationVariant, edgeVariant, patternTag)
            }
        }
    }

    /**
     * Solid silhouette masks with distinct shapes and clean rectangular Beginner grid.
     */
    private fun buildAllowedMaskArray(
        rows: Int,
        cols: Int,
        shape: BoardShape,
        rotationVariant: Int
    ): BooleanArray {
        val allowed = BooleanArray(rows * cols)
        if (shape == BoardShape.CLASSIC) {
            allowed.fill(true)
            return allowed
        }

        val cr = (rows - 1) / 2.0
        val cc = (cols - 1) / 2.0
        val maxR = (rows / 2.0).coerceAtLeast(1.0)
        val maxC = (cols / 2.0).coerceAtLeast(1.0)
        var includedCount = 0

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val rawNr = (r - cr) / maxR
                val rawNc = (c - cc) / maxC

                val (nr, nc) = when (rotationVariant % 4) {
                    1 -> Pair(-rawNc, rawNr)
                    2 -> Pair(-rawNr, -rawNc)
                    3 -> Pair(rawNc, -rawNr)
                    else -> Pair(rawNr, rawNc)
                }

                val include = when (shape) {
                    BoardShape.CLASSIC -> true
                    BoardShape.HEART -> {
                        val x = nc * 1.05
                        val y = -nr * 1.05
                        val eq = (x * x + y * y - 0.90)
                        (eq * eq * eq - x * x * y * y * y) <= 0.18
                    }
                    BoardShape.FLOWER -> {
                        val rad = hypot(nc, nr)
                        val theta = atan2(nr, nc)
                        val boundary = 0.76 + 0.25 * cos(6.0 * theta)
                        rad <= boundary
                    }
                    BoardShape.LOTUS -> {
                        val rad = hypot(nc, nr)
                        val theta = atan2(nr, nc)
                        val boundary = 0.78 + 0.22 * cos(8.0 * theta)
                        rad <= boundary
                    }
                    BoardShape.TRIANGLE -> {
                        nr >= -0.94 && nr <= 0.94 && abs(nc) <= (nr + 0.98) * 0.68
                    }
                    BoardShape.LEAF -> {
                        val u = (nr + nc) * 0.707
                        val v = (nr - nc) * 0.707
                        val inLeafBody = (u * u) / 1.24 + (v * v) / 0.72 <= 1.0
                        val inStem = nr >= 0.70 && nc >= 0.70
                        inLeafBody || inStem
                    }
                    BoardShape.BUTTERFLY -> {
                        val x = abs(nc)
                        val y = -nr
                        val upperWing = (x >= 0.04 && x <= 0.98 && y >= -0.10 && y <= 0.95 && (x - 0.52) * (x - 0.52) / 0.30 + (y - 0.42) * (y - 0.42) / 0.35 <= 1.12)
                        val lowerWing = (x >= 0.04 && x <= 0.92 && y <= 0.10 && y >= -0.94 && (x - 0.46) * (x - 0.46) / 0.26 + (y + 0.45) * (y + 0.45) / 0.30 <= 1.12)
                        val body = x <= 0.28 && abs(nr) <= 0.88
                        upperWing || lowerWing || body
                    }
                    BoardShape.STAR -> {
                        val rad = hypot(nc, nr)
                        val theta = atan2(nr, nc)
                        val boundary = 0.80 + 0.26 * cos(5.0 * theta - Math.PI / 2.0)
                        rad <= boundary
                    }
                    BoardShape.CLOVER -> {
                        val rad = hypot(nc, nr)
                        val theta = atan2(nr, nc)
                        val boundary = 0.82 + 0.24 * cos(4.0 * theta)
                        rad <= boundary
                    }
                    BoardShape.DIAMOND -> abs(nr) + abs(nc) <= 1.26
                    BoardShape.CROWN -> {
                        val inBase = nr >= 0.02 && abs(nc) <= 0.96
                        val leftPeak = nr >= -0.86 && nc <= -0.28 && abs(nc + 0.64) <= (nr + 0.92) * 0.56
                        val midPeak = nr >= -0.96 && abs(nc) <= (nr + 0.98) * 0.50
                        val rightPeak = nr >= -0.86 && nc >= 0.28 && abs(nc - 0.64) <= (nr + 0.92) * 0.56
                        inBase || leftPeak || midPeak || rightPeak
                    }
                    BoardShape.HEXAGON -> abs(nc) <= 0.95 && (abs(nr) + abs(nc) * 0.50) <= 1.15
                    BoardShape.SHIELD -> {
                        val upper = nr <= 0.24 && abs(nc) <= 0.94 && nr >= -0.95
                        val lower = nr > 0.24 && nr <= 0.96 && abs(nc) <= (1.04 - (nr - 0.24) * 1.02)
                        upper || lower
                    }
                    BoardShape.PINE -> {
                        val topTier = nr in -0.96..-0.22 && abs(nc) <= (nr + 0.98) * 0.84
                        val midTier = nr in -0.44..0.26 && abs(nc) <= (nr + 0.68) * 0.96
                        val botTier = nr in 0.04..0.70 && abs(nc) <= (nr + 0.36) * 1.08
                        val trunk = nr in 0.64..0.96 && abs(nc) <= 0.28
                        topTier || midTier || botTier || trunk
                    }
                    BoardShape.PINWHEEL -> {
                        val rad = hypot(nc, nr)
                        val theta = atan2(nr, nc)
                        val arms = sin(4.0 * theta + rad * 1.8) > -0.45
                        rad <= 1.02 && (rad <= 0.52 || arms)
                    }
                    BoardShape.OCTAGON -> {
                        abs(nr) <= 0.96 && abs(nc) <= 0.96 && (abs(nr) + abs(nc)) <= 1.48
                    }
                    BoardShape.CASTLE -> {
                        val body = nr >= -0.44 && nr <= 0.96 && abs(nc) <= 0.90
                        val battlements = nr >= -0.94 && nr < -0.44 &&
                            (abs(nc) <= 0.28 || (abs(nc) in 0.44..0.90))
                        body || battlements
                    }
                }

                if (include) {
                    allowed[r * cols + c] = true
                    includedCount++
                }
            }
        }

        if (includedCount < 16) {
            allowed.fill(true)
        }
        return allowed
    }

    /**
     * Reverse-Peeling Progressive Generator:
     * Places arrows in reverse escape order (guaranteeing 100% solvability), stops at targetCount,
     * respects maxBends & length distribution per tier, and verifies every board via PuzzleSolver.
     */
    private fun generateGuaranteedSolvableWindingArrows(
        rows: Int,
        cols: Int,
        targetCount: Int,
        minPathLen: Int,
        maxPathLen: Int,
        maxBends: Int,
        blockingAffinity: Int,
        shape: BoardShape,
        rotationVariant: Int,
        @Suppress("UNUSED_PARAMETER") edgeVariant: Int,
        seed: Long
    ): List<ArrowTile> {
        var bestValid: List<ArrowTile> = emptyList()
        val maxAttempts = if (targetCount <= 50) 4 else 2

        for (attempt in 0 until maxAttempts) {
            val candidate = generateSingleAttempt(
                rows = rows,
                cols = cols,
                targetCount = targetCount,
                minPathLen = minPathLen,
                maxPathLen = maxPathLen,
                maxBends = maxBends,
                blockingAffinity = blockingAffinity,
                shape = shape,
                rotationVariant = rotationVariant,
                seed = seed + attempt * 7919L
            )
            if (PuzzleSolver.verifySolvable(candidate, rows, cols) != null) {
                if (candidate.size >= targetCount || (targetCount > 50 && candidate.size >= (targetCount * 96) / 100)) {
                    return candidate
                }
                if (candidate.size > bestValid.size) {
                    bestValid = candidate
                }
            }
        }

        if (bestValid.isNotEmpty()) {
            return bestValid
        }

        return listOf(
            ArrowTile(
                id = 1,
                path = listOf(GridCell(1, 1), GridCell(1, 2)),
                direction = Direction.RIGHT,
                toneIndex = 0
            ),
            ArrowTile(
                id = 2,
                path = listOf(GridCell(3, 2), GridCell(2, 2)),
                direction = Direction.UP,
                toneIndex = 1
            )
        )
    }

    private fun generateSingleAttempt(
        rows: Int,
        cols: Int,
        targetCount: Int,
        minPathLen: Int,
        maxPathLen: Int,
        maxBends: Int,
        blockingAffinity: Int,
        shape: BoardShape,
        rotationVariant: Int,
        seed: Long
    ): List<ArrowTile> {
        val rng = Random(seed)
        val totalCells = rows * cols
        val allowedMask = buildAllowedMaskArray(rows, cols, shape, rotationVariant)
        val occupied = BooleanArray(totalCells)
        val activeEscapeRays = BooleanArray(totalCells)
        val selfExitRay = BooleanArray(totalCells)
        val selfSet = BooleanArray(totalCells)
        val placed = ArrayList<ArrowTile>(targetCount)
        val directions = Direction.entries

        val cr = (rows - 1) / 2.0
        val cc = (cols - 1) / 2.0
        val centerPool = ArrayList<Int>(totalCells)
        for (idx in 0 until totalCells) {
            if (allowedMask[idx]) centerPool.add(idx)
        }
        var freeCells = centerPool.size

        val centerKeys = DoubleArray(totalCells) { idx ->
            val r = idx / cols
            val c = idx % cols
            val dr = r - cr
            val dc = c - cc
            dr * dr + dc * dc + rng.nextDouble(0.0, 9.0)
        }

        var passIndex = 0
        var nextId = 1

        while (passIndex < 4 && placed.size < targetCount && centerPool.isNotEmpty()) {
            if (passIndex == 0) {
                centerPool.sortBy { centerKeys[it] }
            } else {
                centerPool.shuffle(rng)
            }

            var placedAnyThisPass = false
            val minAcceptLen = when (passIndex) {
                0, 1 -> minPathLen.coerceAtLeast(2)
                else -> 2
            }

            for (headIdx in centerPool) {
                if (occupied[headIdx]) continue
                if (placed.size >= targetCount) break

                val remainingTargets = (targetCount - placed.size).coerceAtLeast(1)
                val avgBudget = (freeCells / remainingTargets).coerceAtLeast(2)
                val budgetCap = when {
                    targetCount < 15 -> maxPathLen
                    avgBudget >= 7 -> maxPathLen
                    avgBudget >= 5 -> minOf(maxPathLen, avgBudget + 2)
                    avgBudget == 4 -> minOf(maxPathLen, 5)
                    else -> minOf(maxPathLen, 3)
                }

                val headR = headIdx / cols
                val headC = headIdx % cols
                var bestPath: List<GridCell>? = null
                var bestDir = Direction.RIGHT
                var bestScore = Int.MIN_VALUE

                val startDir = rng.nextInt(4)
                for (k in 0 until 4) {
                    val dir = directions[(startDir + k) and 3]
                    selfExitRay.fill(false)
                    var rr = headR + dir.dRow
                    var ccStep = headC + dir.dCol
                    var rayLen = 0
                    var rayClear = true
                    while (rr in 0 until rows && ccStep in 0 until cols) {
                        val rayIdx = rr * cols + ccStep
                        if (occupied[rayIdx]) {
                            rayClear = false
                            break
                        }
                        selfExitRay[rayIdx] = true
                        rayLen++
                        rr += dir.dRow
                        ccStep += dir.dCol
                    }
                    if (!rayClear) continue

                    // Mix short, medium, and long arrows smoothly within board budget
                    val localMin = minOf(minAcceptLen, budgetCap)
                    val safeMax = budgetCap.coerceAtLeast(localMin + 1)
                    val midLen = ((localMin + safeMax) / 2).coerceAtLeast(localMin)
                    val desiredLen = when {
                        passIndex >= 3 -> rng.nextInt(2, localMin.coerceAtLeast(3) + 1)
                        passIndex == 2 -> rng.nextInt(localMin, midLen + 1)
                        budgetCap >= 9 && rng.nextInt(4) == 0 -> rng.nextInt(2, 5)
                        rng.nextInt(3) == 0 -> rng.nextInt(midLen, safeMax + 1)
                        else -> rng.nextInt(localMin, midLen + 1)
                    }

                    val tailToHead = growWindingPathFast(
                        headRow = headR,
                        headCol = headC,
                        rows = rows,
                        cols = cols,
                        direction = dir,
                        desiredLen = desiredLen,
                        maxBends = maxBends,
                        allowedMask = allowedMask,
                        occupied = occupied,
                        selfExitRay = selfExitRay,
                        selfSet = selfSet,
                        activeEscapeRays = activeEscapeRays,
                        rng = rng
                    )

                    if (tailToHead.size < localMin) continue

                    var blocksCount = 0
                    var bends = 0
                    for (cell in tailToHead) {
                        if (activeEscapeRays[cell.row * cols + cell.col]) blocksCount++
                    }
                    for (i in 1 until tailToHead.size - 1) {
                        val d1r = tailToHead[i].row - tailToHead[i - 1].row
                        val d2r = tailToHead[i + 1].row - tailToHead[i].row
                        if (d1r != d2r) bends++
                    }

                    val lengthWeight = if (maxPathLen <= 4) 2 else 4
                    val score = blocksCount * blockingAffinity +
                        tailToHead.size * lengthWeight +
                        bends * 3 +
                        (if (rayLen >= 1) 2 else 0) +
                        rng.nextInt(4)

                    if (score > bestScore) {
                        bestScore = score
                        bestPath = tailToHead
                        bestDir = dir
                    }
                }

                if (bestPath != null) {
                    val bestTile = ArrowTile(
                        id = nextId,
                        path = bestPath,
                        direction = bestDir,
                        toneIndex = nextId % 4
                    )
                    placed.add(bestTile)
                    nextId++
                    placedAnyThisPass = true
                    freeCells -= bestPath.size
                    for (cell in bestPath) {
                        occupied[cell.row * cols + cell.col] = true
                    }
                    var rr = bestTile.headRow + bestTile.direction.dRow
                    var ccStep = bestTile.headCol + bestTile.direction.dCol
                    while (rr in 0 until rows && ccStep in 0 until cols) {
                        activeEscapeRays[rr * cols + ccStep] = true
                        rr += bestTile.direction.dRow
                        ccStep += bestTile.direction.dCol
                    }
                }
            }

            if (placed.size >= targetCount || (!placedAnyThisPass && passIndex >= 2)) break
            centerPool.removeAll { occupied[it] }
            passIndex++
        }

        return placed
            .sortedWith(compareBy({ it.headRow }, { it.headCol }))
            .mapIndexed { index, arrow -> arrow.copy(id = index + 1) }
    }

    private fun growWindingPathFast(
        headRow: Int,
        headCol: Int,
        rows: Int,
        cols: Int,
        direction: Direction,
        desiredLen: Int,
        maxBends: Int,
        allowedMask: BooleanArray,
        occupied: BooleanArray,
        selfExitRay: BooleanArray,
        selfSet: BooleanArray,
        activeEscapeRays: BooleanArray,
        rng: Random
    ): List<GridCell> {
        val reversedPath = ArrayList<GridCell>(desiredLen)
        val headCell = GridCell(headRow, headCol)
        reversedPath.add(headCell)
        val touchedIndices = IntArray(desiredLen)
        var touchedCount = 0

        fun markSelf(idx: Int) {
            selfSet[idx] = true
            if (touchedCount < touchedIndices.size) {
                touchedIndices[touchedCount++] = idx
            }
        }

        fun clearSelf() {
            for (i in 0 until touchedCount) {
                selfSet[touchedIndices[i]] = false
            }
        }

        markSelf(headRow * cols + headCol)

        val neckR = headRow - direction.dRow
        val neckC = headCol - direction.dCol
        if (neckR !in 0 until rows || neckC !in 0 until cols) {
            clearSelf()
            return reversedPath
        }
        val neckIdx = neckR * cols + neckC
        if (!allowedMask[neckIdx] || occupied[neckIdx] || selfExitRay[neckIdx]) {
            clearSelf()
            return reversedPath
        }
        reversedPath.add(GridCell(neckR, neckC))
        markSelf(neckIdx)

        var prevStepDir = direction.opposite()
        var currentBends = 0
        val dirs = Direction.entries

        while (reversedPath.size < desiredLen) {
            val tip = reversedPath.last()
            var bestRow = -1
            var bestCol = -1
            var bestDir: Direction? = null
            var bestScore = Int.MIN_VALUE

            for (stepDir in dirs) {
                val isTurn = stepDir != prevStepDir
                if (isTurn && currentBends >= maxBends) continue

                val nr = tip.row + stepDir.dRow
                val nc = tip.col + stepDir.dCol
                if (nr !in 0 until rows || nc !in 0 until cols) continue
                val nIdx = nr * cols + nc
                if (!allowedMask[nIdx] || occupied[nIdx] || selfSet[nIdx] || selfExitRay[nIdx]) continue

                val turnScore = if (maxBends >= 2 && isTurn) 4 else if (!isTurn) 3 else 1
                val rayScore = if (activeEscapeRays[nIdx]) 5 else 0
                var neighborHug = 0
                for (d in dirs) {
                    val ar = nr + d.dRow
                    val ac = nc + d.dCol
                    if (ar !in 0 until rows || ac !in 0 until cols) {
                        neighborHug++
                    } else {
                        val aIdx = ar * cols + ac
                        if (!allowedMask[aIdx] || occupied[aIdx] || selfSet[aIdx]) {
                            neighborHug++
                        }
                    }
                }
                val score = turnScore + rayScore + neighborHug * 2 + rng.nextInt(4)
                if (score > bestScore) {
                    bestScore = score
                    bestRow = nr
                    bestCol = nc
                    bestDir = stepDir
                }
            }

            if (bestDir == null) break
            if (bestDir != prevStepDir) {
                currentBends++
            }
            reversedPath.add(GridCell(bestRow, bestCol))
            markSelf(bestRow * cols + bestCol)
            prevStepDir = bestDir
        }

        clearSelf()
        return reversedPath.reversed()
    }
}
