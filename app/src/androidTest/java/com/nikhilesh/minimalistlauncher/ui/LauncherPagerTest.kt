package com.nikhilesh.minimalistlauncher.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.espresso.Espresso.pressBack
import com.nikhilesh.minimalistlauncher.LauncherUiState
import com.nikhilesh.minimalistlauncher.data.filterApps
import com.nikhilesh.minimalistlauncher.model.LauncherApp
import com.nikhilesh.minimalistlauncher.model.LauncherPalette
import com.nikhilesh.minimalistlauncher.model.LauncherSettings
import com.nikhilesh.minimalistlauncher.model.LauncherTextScale
import com.nikhilesh.minimalistlauncher.ui.theme.MinimalistTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LauncherPagerTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val phone = LauncherApp("phone/.Main", "Phone")
    private val maps = LauncherApp("maps/.Main", "Maps")

    @Test
    fun homeOpensAppListAndSettings() {
        setLauncherContent()

        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeRule.onNodeWithText("All apps  →").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("app_list_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithText("Appearance").assertIsDisplayed()
    }

    @Test
    fun swipesAndBackReturnToHome() {
        var webSearchOpened = false
        setLauncherContent(onOpenWebSearch = { webSearchOpened = true })

        composeRule.onNodeWithTag("home_screen").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("app_list_screen").assertIsDisplayed()

        pressBack()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()

        composeRule.onNodeWithTag("home_screen").performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.runOnIdle { assertTrue(webSearchOpened) }
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()

        composeRule.onNodeWithText("All apps  →").performClick()
        composeRule.waitForIdle()
        pressBack()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
    }

    @Test
    fun searchFiltersTheAppList() {
        val apps = listOf(maps, phone)
        composeRule.setContent {
            var query by remember { mutableStateOf("") }
            val settings = LauncherSettings(onboardingComplete = true)
            MinimalistTheme(settings) {
                AppListScreen(
                    apps = filterApps(apps, query),
                    favoriteIds = emptySet(),
                    favorites = emptyList(),
                    query = query,
                    palette = LauncherPalette.INK,
                    textScale = LauncherTextScale.STANDARD,
                    onQueryChange = { query = it },
                    onLaunch = {},
                    onToggleFavorite = {},
                    onOpenAppInfo = {},
                    onMoveFavorite = { _, _ -> },
                    onSetPalette = {},
                    onSetTextScale = {},
                    onOpenHomeSettings = {},
                )
            }
        }

        composeRule.onNodeWithTag("app_search").performTextInput("map")
        composeRule.onNodeWithText("Maps").assertIsDisplayed()
        composeRule.onNodeWithText("Phone").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Clear search").performClick()
        composeRule.onNodeWithText("Phone").assertIsDisplayed()

        composeRule.onNodeWithTag("app_search").performTextInput("map")
        composeRule.onNodeWithText("Maps").performClick()
        composeRule.onNodeWithText("Phone").assertIsDisplayed()
    }

    @Test
    fun favoriteOnboardingSupportsSearchAndSkip() {
        var completed = false
        val settings = LauncherSettings()
        composeRule.setContent {
            MinimalistTheme(settings) {
                FavoriteOnboarding(
                    apps = listOf(maps, phone),
                    selectedIds = emptyList(),
                    onToggleFavorite = {},
                    onContinue = { completed = true },
                )
            }
        }

        composeRule.onNodeWithTag("onboarding_search").performTextInput("map")
        composeRule.onNodeWithText("Maps").assertIsDisplayed()
        composeRule.onNodeWithText("Phone").assertDoesNotExist()
        composeRule.onNodeWithText("Skip").performClick()
        composeRule.runOnIdle { assertTrue(completed) }
    }

    @Test
    fun emptyFavoritesExplainHowToAddApps() {
        val settings = LauncherSettings(onboardingComplete = true)
        composeRule.setContent {
            MinimalistTheme(settings) {
                HomeScreen(
                    favorites = emptyList(),
                    onLaunch = {},
                    onRemoveFavorite = {},
                    onOpenAppInfo = {},
                    onShowApps = {},
                    onOpenWebSearch = {},
                )
            }
        }

        composeRule.onNodeWithText("No favorites yet").assertIsDisplayed()
        composeRule.onNodeWithText("Swipe left or open All apps to add some.").assertIsDisplayed()
    }

    @Test
    fun longPressOffersFavoriteAndAppInfoActions() {
        val settings = LauncherSettings(onboardingComplete = true)
        composeRule.setContent {
            MinimalistTheme(settings) {
                HomeScreen(
                    favorites = listOf(phone),
                    onLaunch = {},
                    onRemoveFavorite = {},
                    onOpenAppInfo = {},
                    onShowApps = {},
                    onOpenWebSearch = {},
                )
            }
        }

        composeRule.onNodeWithText("Phone").performTouchInput { longClick() }
        composeRule.onNodeWithText("Remove from favorites").assertIsDisplayed()
        composeRule.onNodeWithText("App info").assertIsDisplayed()
    }

    private fun setLauncherContent(onOpenWebSearch: () -> Unit = {}) {
        val settings = LauncherSettings(
            favoriteIds = listOf(phone.id),
            onboardingComplete = true,
        )
        val state = LauncherUiState(
            apps = listOf(maps, phone),
            filteredApps = listOf(maps, phone),
            favorites = listOf(phone),
            settings = settings,
        )

        composeRule.setContent {
            MinimalistTheme(state.settings) {
                LauncherPager(
                    uiState = state,
                    onQueryChange = {},
                    onLaunch = {},
                    onToggleFavorite = {},
                    onOpenAppInfo = {},
                    onMoveFavorite = { _, _ -> },
                    onSetPalette = {},
                    onSetTextScale = {},
                    onOpenHomeSettings = {},
                    onOpenWebSearch = onOpenWebSearch,
                )
            }
        }
    }
}
