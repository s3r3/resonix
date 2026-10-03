package com.resonix.media.feature.settings.navigation

import com.resonix.media.feature.settings.Setting
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Assert.assertTrue

class SettingsNavigationTest {
    @Test
    fun settingValuesContainAllPlannedScreens() {
        val values = Setting.entries
        assertEquals(8, values.size)
        assertTrue(Setting.APPEARANCE in values)
        assertTrue(Setting.ABOUT in values)
    }
}
