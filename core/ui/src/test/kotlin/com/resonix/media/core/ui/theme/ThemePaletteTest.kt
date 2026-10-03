package com.resonix.media.core.ui.theme

import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePaletteTest {

    @Test
    fun `the light background is brighter than the dark background`() {
        assertTrue(backgroundLight.luminance() > backgroundDark.luminance())
    }

    @Test
    fun `the light foreground is darker than its background`() {
        assertTrue(onBackgroundLight.luminance() < backgroundLight.luminance())
        assertTrue(onBackgroundDark.luminance() > backgroundDark.luminance())
    }

    @Test
    fun `raising the contrast level darkens the light primary`() {
        val default = primaryLight.luminance()
        val medium = primaryLightMediumContrast.luminance()
        val high = primaryLightHighContrast.luminance()

        assertTrue("$medium !< $default", medium < default)
        assertTrue("$high !< $medium", high < medium)
    }

    @Test
    fun `raising the contrast level brightens the dark primary`() {
        val default = primaryDark.luminance()
        val medium = primaryDarkMediumContrast.luminance()
        val high = primaryDarkHighContrast.luminance()

        assertTrue("$medium !> $default", medium > default)
        assertTrue("$high !> $medium", high > medium)
    }

    @Test
    fun `the pure black palette is darker than the standard dark palette`() {
        assertNotEquals(backgroundDark, backgroundPureBlack)
        assertTrue(backgroundPureBlack.luminance() < backgroundDark.luminance())
    }
}