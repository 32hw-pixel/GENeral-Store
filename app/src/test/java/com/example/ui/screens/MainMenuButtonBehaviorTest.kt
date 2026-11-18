package com.example.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.data.defaultConversations
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppTranslations
import com.example.util.LocalAppStrings
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MainMenuButtonBehaviorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `guest session displays auth button and hides about button`() {
        var authClicked = false
        var aboutClicked = false

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides AppTranslations.forCode("tr")) {
                MyApplicationTheme(themeMode = AppThemeMode.CYBER_DARK, paperMode = false) {
                    SohbetlerTabContent(
                        conversations = defaultConversations,
                        isCloudConnected = false,
                        isAccountSignedIn = false,
                        cloudHandle = null,
                        cloudAuthLoading = false,
                        onGoogleConnect = {},
                        onOpenNewChat = {},
                        onOpenSelfChat = {},
                        onConversationClick = {},
                        onGoToAi = {},
                        onOpenAuth = { authClicked = true },
                        onOpenAbout = { aboutClicked = true },
                        onOpenChannel = {},
                        onOpenCloudSetup = {},
                        onOpenOfflineRadar = {}
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_about_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("home_auth_button")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        assertTrue(authClicked)
        assertTrue(!aboutClicked)
    }

    @Test
    fun `signed in user or google user hides auth button and displays about button`() {
        var authClicked = false
        var aboutClicked = false

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides AppTranslations.forCode("tr")) {
                MyApplicationTheme(themeMode = AppThemeMode.CYBER_DARK, paperMode = false) {
                    SohbetlerTabContent(
                        conversations = defaultConversations,
                        isCloudConnected = true,
                        isAccountSignedIn = true,
                        cloudHandle = "demo_user",
                        cloudAuthLoading = false,
                        onGoogleConnect = {},
                        onOpenNewChat = {},
                        onOpenSelfChat = {},
                        onConversationClick = {},
                        onGoToAi = {},
                        onOpenAuth = { authClicked = true },
                        onOpenAbout = { aboutClicked = true },
                        onOpenChannel = {},
                        onOpenCloudSetup = {},
                        onOpenOfflineRadar = {}
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_auth_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("home_about_button")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        assertTrue(aboutClicked)
        assertTrue(!authClicked)
    }
}
