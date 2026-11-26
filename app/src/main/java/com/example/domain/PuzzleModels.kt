package com.example.domain

enum class Direction(val dRow: Int, val dCol: Int, val symbol: String) {
    UP(-1, 0, "↑"),
    DOWN(1, 0, "↓"),
    LEFT(0, -1, "←"),
    RIGHT(0, 1, "→");

    fun opposite(): Direction = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
    }
}

data class GridCell(val row: Int, val col: Int)

/**
 * A winding polyline arrow occupying a sequence of contiguous orthogonal grid cells
 * from [path].first() (tail) to [path].last() (head), pointing in [direction].
 */
data class ArrowTile(
    val id: Int,
    val path: List<GridCell>,
    val direction: Direction,
    val toneIndex: Int = 0
) {
    constructor(
        id: Int,
        headRow: Int,
        headCol: Int,
        direction: Direction,
        length: Int = 1,
        toneIndex: Int = 0
    ) : this(
        id = id,
        path = List(length.coerceAtLeast(1)) { idx ->
            val back = (length.coerceAtLeast(1) - 1) - idx
            GridCell(
                row = headRow - back * direction.dRow,
                col = headCol - back * direction.dCol
            )
        },
        direction = direction,
        toneIndex = toneIndex
    )

    val headCell: GridCell get() = path.last()
    val headRow: Int get() = headCell.row
    val headCol: Int get() = headCell.col
    val length: Int get() = path.size
    val cells: List<GridCell> get() = path

    fun occupies(row: Int, col: Int): Boolean {
        for (cell in path) {
            if (cell.row == row && cell.col == col) return true
        }
        return false
    }
}

enum class DifficultyTier(
    val title: String,
    val levelRange: String,
    val badgeColorHex: Long
) {
    VERY_EASY("Beginner", "1–5", 0xFF16A34A),
    EASY_MEDIUM("Easy", "6–15", 0xFF0288D1),
    MEDIUM("Medium", "16–30", 0xFF1565C0),
    HARD("Hard", "31–50", 0xFFF59E0B),
    VERY_HARD("Expert", "51–100", 0xFFEA580C),
    ADVANCED("Master", "101+", 0xFF7C3AED);

    companion object {
        fun forLevel(levelNumber: Int): DifficultyTier = when {
            levelNumber <= 5 -> VERY_EASY
            levelNumber <= 15 -> EASY_MEDIUM
            levelNumber <= 30 -> MEDIUM
            levelNumber <= 50 -> HARD
            levelNumber <= 100 -> VERY_HARD
            else -> ADVANCED
        }
    }
}

enum class GameMode {
    TUTORIAL,
    CAMPAIGN,
    DAILY_CHALLENGE,
    TOURNAMENT
}

data class PuzzleLevel(
    val id: String,
    val levelNumber: Int,
    val title: String,
    val rows: Int,
    val cols: Int,
    val arrows: List<ArrowTile>,
    val difficulty: DifficultyTier,
    val mode: GameMode,
    val patternTag: String = "Classic",
    val dailyEpochDay: Long? = null,
    val tournamentStage: Int? = null
)

data class PathInspection(
    val arrowId: Int,
    val isClear: Boolean,
    val rayCells: List<GridCell>,
    val blockerArrowId: Int?,
    val blockerCell: GridCell?
)
