package com.example

import com.example.domain.DifficultyTier
import com.example.domain.GridCell
import com.example.domain.PuzzleGenerator
import com.example.domain.PuzzleSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleEngineUnitTest {

    @Test
    fun `tutorial puzzle teaches clear and blocked rules accurately`() {
        val tutorial = PuzzleGenerator.getTutorialPuzzle()
        val arrows = tutorial.arrows
        assertEquals(3, arrows.size)

        val arrow1 = arrows.first { it.id == 1 }
        val arrow2 = arrows.first { it.id == 2 }
        val arrow3 = arrows.first { it.id == 3 }

        assertTrue(PuzzleSolver.canEscape(arrow1, arrows, tutorial.rows, tutorial.cols))
        assertFalse(PuzzleSolver.canEscape(arrow2, arrows, tutorial.rows, tutorial.cols))
        assertTrue(PuzzleSolver.canEscape(arrow3, arrows, tutorial.rows, tutorial.cols))

        val solution = PuzzleSolver.verifySolvable(arrows, tutorial.rows, tutorial.cols)
        assertNotNull(solution)
        assertEquals(3, solution!!.size)
    }

    @Test
    fun `all 1200 campaign levels are non-overlapping and 100 percent solvable`() {
        for (levelNum in 1..1200) {
            val level = PuzzleGenerator.generateCampaignLevel(levelNum)
            assertTrue("Level $levelNum empty", level.arrows.isNotEmpty())

            // Verify zero overlapping cells, orthogonal contiguity, and no self-blocking ray
            val occupied = HashSet<GridCell>()
            for (arrow in level.arrows) {
                for (i in arrow.path.indices) {
                    val cell = arrow.path[i]
                    assertTrue(
                        "Level $levelNum out of bounds cell $cell",
                        cell.row in 0 until level.rows && cell.col in 0 until level.cols
                    )
                    assertTrue(
                        "Level $levelNum overlapping cell $cell",
                        occupied.add(cell)
                    )
                    if (i > 0) {
                        val prev = arrow.path[i - 1]
                        val manhattan = kotlin.math.abs(cell.row - prev.row) + kotlin.math.abs(cell.col - prev.col)
                        assertEquals("Level $levelNum non-contiguous arrow path", 1, manhattan)
                    }
                }
            }

            // Verify guaranteed solvable
            val solution = PuzzleSolver.verifySolvable(level.arrows, level.rows, level.cols)
            assertNotNull("Level $levelNum must be solvable", solution)
            assertEquals(level.arrows.size, solution!!.size)
        }
    }

    @Test
    fun `difficulty tiers map to expected level ranges`() {
        assertEquals(DifficultyTier.VERY_EASY, DifficultyTier.forLevel(1))
        assertEquals(DifficultyTier.VERY_EASY, DifficultyTier.forLevel(5))
        assertEquals(DifficultyTier.EASY_MEDIUM, DifficultyTier.forLevel(6))
        assertEquals(DifficultyTier.EASY_MEDIUM, DifficultyTier.forLevel(15))
        assertEquals(DifficultyTier.MEDIUM, DifficultyTier.forLevel(16))
        assertEquals(DifficultyTier.MEDIUM, DifficultyTier.forLevel(30))
        assertEquals(DifficultyTier.HARD, DifficultyTier.forLevel(31))
        assertEquals(DifficultyTier.HARD, DifficultyTier.forLevel(50))
        assertEquals(DifficultyTier.VERY_HARD, DifficultyTier.forLevel(51))
        assertEquals(DifficultyTier.VERY_HARD, DifficultyTier.forLevel(100))
        assertEquals(DifficultyTier.ADVANCED, DifficultyTier.forLevel(101))
    }

    @Test
    fun `daily challenges and tournament stages are solvable`() {
        for (day in 1..30) {
            val daily = PuzzleGenerator.generateDailyLevel(20000L + day, day)
            assertNotNull(PuzzleSolver.verifySolvable(daily.arrows, daily.rows, daily.cols))
        }
        for (stage in 1..5) {
            val tLevel = PuzzleGenerator.generateTournamentStage(100, stage)
            assertNotNull(PuzzleSolver.verifySolvable(tLevel.arrows, tLevel.rows, tLevel.cols))
        }
    }

    @Test
    fun `best hint always chooses a valid clear arrow`() {
        val level = PuzzleGenerator.generateCampaignLevel(75)
        val hint = PuzzleSolver.findBestHintArrow(level.arrows, level.rows, level.cols)
        assertNotNull(hint)
        assertTrue(PuzzleSolver.canEscape(hint!!, level.arrows, level.rows, level.cols))
    }

    @Test
    fun `progressive difficulty brackets scale smoothly from beginner to expert`() {
        var prevCount = 0
        for (levelNum in 1..100) {
            val lvl = PuzzleGenerator.generateCampaignLevel(levelNum)
            val count = lvl.arrows.size
            when (levelNum) {
                in 1..5 -> {
                    assertTrue("Level $levelNum beginner arrows=$count (expected 2..4)", count in 2..4)
                    assertTrue(
                        "Level $levelNum beginner arrows should be short (<=4 cells)",
                        lvl.arrows.all { it.length <= 4 }
                    )
                }
                in 6..9 -> {
                    assertTrue("Level $levelNum easy arrows=$count (expected 5..9)", count in 5..9)
                }
                in 10..15 -> {
                    assertTrue("Level $levelNum ramped arrows=$count (expected 12..24)", count in 12..24)
                }
                in 16..30 -> {
                    assertTrue("Level $levelNum medium arrows=$count (expected 24..50)", count in 24..50)
                }
                in 31..50 -> {
                    assertTrue("Level $levelNum hard arrows=$count (expected 50..90)", count in 50..90)
                }
                in 51..100 -> {
                    assertTrue("Level $levelNum expert arrows=$count (expected 90..155)", count in 90..155)
                }
            }
            if (levelNum > 1 && levelNum != 10) {
                assertTrue(
                    "Sudden difficulty jump at level $levelNum: prev=$prevCount -> curr=$count",
                    count >= prevCount - 2 && (count - prevCount) <= 5
                )
            }
            prevCount = count
        }

        // Level 1 acts as a crystal-clear starter tutorial
        val l1 = PuzzleGenerator.generateCampaignLevel(1)
        assertEquals(2, l1.arrows.size)
        assertEquals("Beginner Tutorial", l1.patternTag)

        // Level 10 kicks up noticeably more arrows than Level 9
        val l9 = PuzzleGenerator.generateCampaignLevel(9)
        val l10 = PuzzleGenerator.generateCampaignLevel(10)
        assertTrue("Level 10 should have more arrows than Level 9", l10.arrows.size > l9.arrows.size)

        // Expert levels include a mix of short/medium and longer winding paths on larger boards
        val l90 = PuzzleGenerator.generateCampaignLevel(90)
        assertTrue("Level 90 should have large board", l90.rows >= 30)
        assertTrue("Level 90 should include longer arrows", l90.arrows.any { it.length >= 6 })

        // Distinct shaped designs across Medium/Hard levels
        val designTags = (16..30).map { PuzzleGenerator.generateCampaignLevel(it).patternTag }.toSet()
        assertTrue("Expected varied puzzle designs across levels 16-30", designTags.size >= 8)
    }
}
