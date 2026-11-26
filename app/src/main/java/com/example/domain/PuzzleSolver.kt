package com.example.domain

object PuzzleSolver {

    fun inspectArrowPath(
        arrow: ArrowTile,
        remainingArrows: List<ArrowTile>,
        rows: Int,
        cols: Int
    ): PathInspection {
        val occ = buildOccupancy(remainingArrows, rows, cols)
        return inspectWithOccupancy(arrow, occ, rows, cols)
    }

    fun canEscape(
        arrow: ArrowTile,
        remainingArrows: List<ArrowTile>,
        rows: Int,
        cols: Int
    ): Boolean {
        val occ = buildOccupancy(remainingArrows, rows, cols)
        return isClearWithOccupancy(arrow, occ, rows, cols)
    }

    fun findClearArrows(
        remainingArrows: List<ArrowTile>,
        rows: Int,
        cols: Int
    ): List<ArrowTile> {
        if (remainingArrows.isEmpty()) return emptyList()
        val occ = buildOccupancy(remainingArrows, rows, cols)
        return remainingArrows.filter { isClearWithOccupancy(it, occ, rows, cols) }
    }

    fun findBestHintArrow(
        remainingArrows: List<ArrowTile>,
        rows: Int,
        cols: Int
    ): ArrowTile? {
        if (remainingArrows.isEmpty()) return null
        val occ = buildOccupancy(remainingArrows, rows, cols)
        val clearArrows = remainingArrows.filter { isClearWithOccupancy(it, occ, rows, cols) }
        if (clearArrows.isEmpty()) return null
        if (clearArrows.size == 1) return clearArrows.first()

        // Prefer the clear arrow whose removal frees the most currently blocked arrows.
        var bestArrow = clearArrows.first()
        var bestFreed = -1
        for (candidate in clearArrows) {
            setArrowOccupancy(candidate, occ, rows, cols, value = 0)
            var clearCountAfter = 0
            for (other in remainingArrows) {
                if (other.id != candidate.id && isClearWithOccupancy(other, occ, rows, cols)) {
                    clearCountAfter++
                }
            }
            setArrowOccupancy(candidate, occ, rows, cols, value = candidate.id)
            if (clearCountAfter > bestFreed) {
                bestFreed = clearCountAfter
                bestArrow = candidate
            }
        }
        return bestArrow
    }

    /**
     * Because Arrow Escape is confluent (removing a clear arrow never creates a new obstacle),
     * a greedy topological peeling pass returns a complete escape order if and only if
     * the puzzle is 100% solvable.
     */
    fun verifySolvable(
        arrows: List<ArrowTile>,
        rows: Int,
        cols: Int
    ): List<Int>? {
        if (arrows.isEmpty()) return emptyList()
        val occ = buildOccupancy(arrows, rows, cols)
        val remaining = ArrayList<ArrowTile>(arrows)
        val solutionOrder = ArrayList<Int>(arrows.size)

        while (remaining.isNotEmpty()) {
            var foundIdx = -1
            for (i in remaining.indices) {
                if (isClearWithOccupancy(remaining[i], occ, rows, cols)) {
                    foundIdx = i
                    break
                }
            }
            if (foundIdx == -1) {
                return null
            }
            val removed = remaining.removeAt(foundIdx)
            setArrowOccupancy(removed, occ, rows, cols, value = 0)
            solutionOrder.add(removed.id)
        }
        return solutionOrder
    }

    private fun buildOccupancy(
        arrows: List<ArrowTile>,
        rows: Int,
        cols: Int
    ): IntArray {
        val occ = IntArray(rows * cols)
        for (arrow in arrows) {
            setArrowOccupancy(arrow, occ, rows, cols, arrow.id)
        }
        return occ
    }

    private fun setArrowOccupancy(
        arrow: ArrowTile,
        occ: IntArray,
        rows: Int,
        cols: Int,
        value: Int
    ) {
        for (cell in arrow.cells) {
            if (cell.row in 0 until rows && cell.col in 0 until cols) {
                occ[cell.row * cols + cell.col] = value
            }
        }
    }

    private fun isClearWithOccupancy(
        arrow: ArrowTile,
        occ: IntArray,
        rows: Int,
        cols: Int
    ): Boolean {
        var r = arrow.headRow + arrow.direction.dRow
        var c = arrow.headCol + arrow.direction.dCol
        val dr = arrow.direction.dRow
        val dc = arrow.direction.dCol
        val id = arrow.id
        while (r in 0 until rows && c in 0 until cols) {
            val occId = occ[r * cols + c]
            if (occId != 0 && occId != id) {
                return false
            }
            r += dr
            c += dc
        }
        return true
    }

    private fun inspectWithOccupancy(
        arrow: ArrowTile,
        occ: IntArray,
        rows: Int,
        cols: Int
    ): PathInspection {
        val rayCells = ArrayList<GridCell>(maxOf(rows, cols))
        var r = arrow.headRow + arrow.direction.dRow
        var c = arrow.headCol + arrow.direction.dCol
        val dr = arrow.direction.dRow
        val dc = arrow.direction.dCol
        val id = arrow.id

        while (r in 0 until rows && c in 0 until cols) {
            val cell = GridCell(r, c)
            rayCells.add(cell)
            val occId = occ[r * cols + c]
            if (occId != 0 && occId != id) {
                return PathInspection(
                    arrowId = id,
                    isClear = false,
                    rayCells = rayCells,
                    blockerArrowId = occId,
                    blockerCell = cell
                )
            }
            r += dr
            c += dc
        }

        return PathInspection(
            arrowId = id,
            isClear = true,
            rayCells = rayCells,
            blockerArrowId = null,
            blockerCell = null
        )
    }
}
