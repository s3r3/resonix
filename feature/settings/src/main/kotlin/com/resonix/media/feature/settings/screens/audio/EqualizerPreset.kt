package com.resonix.media.feature.settings.screens.audio

import androidx.annotation.StringRes
import com.resonix.media.core.model.PlayerPreferences
import com.resonix.media.core.ui.R

/**
 * Equalizer presets, in decibels per band.
 *
 * Levels are the same ones players have shipped for years: a bass-heavy curve for Rock, a lifted
 * midrange for speech-oriented presets, and so on. [Custom] is not a curve but the state the
 * screen falls back to as soon as a band is moved away from whatever preset is selected.
 */
enum class EqualizerPreset(
    @StringRes val label: Int,
    val levels: List<Int>,
) {
    FLAT(R.string.equalizer_preset_flat, listOf(0, 0, 0, 0, 0)),
    CUSTOM(R.string.equalizer_preset_custom, listOf(0, 0, 0, 0, 0)),
    ROCK(R.string.equalizer_preset_rock, listOf(6, 3, -2, -3, 5)),
    POP(R.string.equalizer_preset_pop, listOf(-1, 3, 5, 3, -1)),
    JAZZ(R.string.equalizer_preset_jazz, listOf(4, 3, 1, 2, 4)),
    CLASSICAL(R.string.equalizer_preset_classical, listOf(5, 4, 3, 3, 5)),
    VOCAL(R.string.equalizer_preset_vocal, listOf(-3, 1, 4, 4, 0)),
    BASS_BOOST(R.string.equalizer_preset_bass_boost, listOf(8, 6, 3, 0, 0)),
    TREBLE_BOOST(R.string.equalizer_preset_treble_boost, listOf(0, 0, 2, 5, 8)),
    ;

    val levelsMillibels: List<Int>
        get() = levels.map { it * MILLIBELS_PER_DECIBEL }

    val levelsDecibels: List<Float>
        get() = levels.map(Int::toFloat)

    companion object {
        private const val MILLIBELS_PER_DECIBEL = 100

        /** The preset the given millibel levels match, or [CUSTOM] when they match nothing. */
        fun match(levels: List<Int>): EqualizerPreset = entries.firstOrNull {
            it != CUSTOM && it.levelsMillibels == levels
        } ?: CUSTOM

        fun fromDecibels(levels: List<Float>): List<Int> = levels.map { level ->
            (level * MILLIBELS_PER_DECIBEL).toInt()
                .coerceIn(PlayerPreferences.EQUALIZER_MIN_LEVEL, PlayerPreferences.EQUALIZER_MAX_LEVEL)
        }
    }
}

/** Centre frequencies of the bands the sliders drive, for their labels. */
val equalizerBandLabels: List<Int> = listOf(
    R.string.equalizer_band_low,
    R.string.equalizer_band_low_mid,
    R.string.equalizer_band_mid,
    R.string.equalizer_band_high_mid,
    R.string.equalizer_band_high,
)