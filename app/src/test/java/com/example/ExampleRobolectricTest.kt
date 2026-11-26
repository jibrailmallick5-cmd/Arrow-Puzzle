package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun readAppNameString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Arrow Puzzle", appName)
    }

    @Test
    fun firstLaunchStartsTutorialAndSkipOpensMainMenu() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            runCatching {
                composeTestRule.onNodeWithTag("tutorial_instruction_card").assertExists()
                true
            }.getOrDefault(false)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Welcome! Let's Learn How to Play.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("skip_tutorial_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            runCatching {
                composeTestRule.onNodeWithTag("menu_new_game_button").assertExists()
                true
            }.getOrDefault(false)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("menu_new_game_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("menu_play_level_badge", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Play Now", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Level 1", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("menu_daily_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("menu_tournament_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("menu_profile_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("menu_levels_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("current_level_badge").assertDoesNotExist()
        composeTestRule.onNodeWithText("Unlimited Mode").assertDoesNotExist()
        composeTestRule.onNodeWithText("Very Easy").assertDoesNotExist()
        composeTestRule.onNodeWithText("3D Arrow Escape Edition").assertDoesNotExist()
    }

    @Test
    fun tutorialCompletionStartsCampaignAndGameplayToolsWork() {
        // Ensure we are in Tutorial (either auto-launched on fresh state, or via Settings -> Replay Tutorial)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            runCatching {
                composeTestRule.onNodeWithTag("tutorial_instruction_card").assertExists()
                true
            }.getOrDefault(false) || runCatching {
                composeTestRule.onNodeWithTag("menu_settings_button").assertExists()
                true
            }.getOrDefault(false)
        }
        composeTestRule.waitForIdle()

        if (runCatching { composeTestRule.onNodeWithTag("menu_settings_button").assertExists(); true }.getOrDefault(false)) {
            composeTestRule.onNodeWithTag("menu_settings_button").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("replay_tutorial_button").performScrollTo().performClick()
            composeTestRule.waitForIdle()
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            runCatching {
                composeTestRule.onNodeWithTag("arrow_tile_1").assertExists()
                true
            }.getOrDefault(false)
        }
        composeTestRule.waitForIdle()

        // Step 1: tap arrow 1 (clear)
        composeTestRule.onNodeWithTag("arrow_tile_1").performClick()
        composeTestRule.waitForIdle()

        // Step 2: tap arrow 3 (clear)
        composeTestRule.onNodeWithTag("arrow_tile_3").performClick()
        composeTestRule.waitForIdle()

        // Step 3: tap arrow 2 (now clear!) -> snake slithers off, then Tutorial Completed!
        composeTestRule.onNodeWithTag("arrow_tile_2").performClick()
        org.robolectric.shadows.ShadowLooper.shadowMainLooper().idleFor(
            700,
            java.util.concurrent.TimeUnit.MILLISECONDS
        )
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            org.robolectric.shadows.ShadowLooper.shadowMainLooper().idleFor(
                100,
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
            runCatching {
                composeTestRule.onNodeWithTag("level_completed_card").assertExists()
                true
            }.getOrDefault(false)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("level_completed_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tutorial Completed!").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("next_level_badge", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Level 1", useUnmergedTree = true).assertIsDisplayed()

        // Proceed automatically to campaign puzzle (showing Arrow Puzzle + Level 1)
        composeTestRule.onNodeWithTag("next_level_button").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            runCatching {
                composeTestRule.onNodeWithTag("hearts_bar").assertExists()
                true
            }.getOrDefault(false)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Arrow Puzzle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("current_running_level_text").assertIsDisplayed()
        composeTestRule.onNodeWithText("Level 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Very Easy").assertDoesNotExist()
        composeTestRule.onNodeWithTag("hearts_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hint_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ruler_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("zoom_controls_bar").assertDoesNotExist()

        // Toggle ruler tool
        composeTestRule.onNodeWithTag("ruler_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Ruler Active — Green lines are clear to escape; Red X marks blockers.").assertIsDisplayed()

        // Use hint tool
        composeTestRule.onNodeWithTag("hint_button").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun navigateDailyTournamentProfileAndSettings() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            runCatching {
                composeTestRule.onNodeWithTag("skip_tutorial_button").assertExists()
                true
            }.getOrDefault(false) || runCatching {
                composeTestRule.onNodeWithTag("menu_daily_button").assertExists()
                true
            }.getOrDefault(false)
        }
        composeTestRule.waitForIdle()

        if (runCatching { composeTestRule.onNodeWithTag("skip_tutorial_button").assertExists(); true }.getOrDefault(false)) {
            composeTestRule.onNodeWithTag("skip_tutorial_button").performClick()
            composeTestRule.waitForIdle()
        }

        // Daily Challenge
        composeTestRule.onNodeWithTag("menu_daily_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("play_today_daily_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("daily_back_button").performClick()
        composeTestRule.waitForIdle()

        // Tournament
        composeTestRule.onNodeWithTag("menu_tournament_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("play_tournament_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("tournament_back_button").performClick()
        composeTestRule.waitForIdle()

        // Profile
        composeTestRule.onNodeWithTag("menu_profile_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Awards & Achievements").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_back_button").performClick()
        composeTestRule.waitForIdle()

        // Settings
        composeTestRule.onNodeWithTag("menu_settings_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("replay_tutorial_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("reset_progress_button").performScrollTo().assertIsDisplayed()
    }
}
