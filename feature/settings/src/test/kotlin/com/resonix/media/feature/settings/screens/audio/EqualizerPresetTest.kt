package com.resonix.media.feature.settings.screens.audio

import com.resonix.media.core.model.PlayerPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EqualizerPresetTest {

    @Test
    fun bandCountMatchesThePlayerPreference() {
        EqualizerPreset.entries.forEach { preset ->
            assertEquals(
                preset.name,
                PlayerPreferences.EQUALIZER_BAND_COUNT,
                preset.levels.size,
            )
        }
    }

    @Test
    fun decibelsConvertToMillibelsWithinTheEffectRange() {
        assertEquals(
            listOf(600, -250, 0, 1200, -1500),
            EqualizerPreset.fromDecibels(listOf(6f, -2.5f, 0f, 12f, -90f)),
        )
        assertEquals(
            listOf(PlayerPreferences.EQUALIZER_MIN_LEVEL, PlayerPreferences.EQUALIZER_MAX_LEVEL),
            EqualizerPreset.fromDecibels(listOf(-99f, 99f)),
        )
    }

    @Test
    fun aPresetIsRecognisedFromItsLevels() {
        assertEquals(EqualizerPreset.FLAT, EqualizerPreset.match(EqualizerPreset.FLAT.levelsMillibels))
        assertEquals(EqualizerPreset.ROCK, EqualizerPreset.match(EqualizerPreset.ROCK.levelsMillibels))
    }

    @Test
    fun levelsMatchingNoPresetReadAsCustom() {
        assertEquals(EqualizerPreset.CUSTOM, EqualizerPreset.match(listOf(1, 2, 3, 4, 5)))
        assertEquals(EqualizerPreset.CUSTOM, EqualizerPreset.match(PlayerPreferences.DEFAULT_EQUALIZER_BANDS.take(2)))
    }

    @Test
    fun customIsNeverSelectedByMatchingItsOwnLevels() {
        assertEquals(EqualizerPreset.FLAT, EqualizerPreset.match(EqualizerPreset.CUSTOM.levelsMillibels))
    }

    @Test
    fun presetLevelsSurviveTheMillibelRoundTrip() {
        EqualizerPreset.entries.filterNot { it == EqualizerPreset.CUSTOM }.forEach { preset ->
            val millibels = preset.levelsMillibels
            assertEquals(preset.name, preset, EqualizerPreset.match(millibels))
            assertTrue(
                preset.name,
                millibels.all { it in PlayerPreferences.EQUALIZER_MIN_LEVEL..PlayerPreferences.EQUALIZER_MAX_LEVEL },
            )
        }
    }
}