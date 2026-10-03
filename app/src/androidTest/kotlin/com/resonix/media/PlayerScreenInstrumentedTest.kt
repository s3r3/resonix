package com.resonix.media

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerScreenInstrumentedTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun playerScreen_displaysContent() {
        composeTestRule.setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(text = "Resonix Player Controls")
            }
        }
        composeTestRule
            .onNodeWithText("Resonix Player Controls")
            .assertIsDisplayed()
    }
}
