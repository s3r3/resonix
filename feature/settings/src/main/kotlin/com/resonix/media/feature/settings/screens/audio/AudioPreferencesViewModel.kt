package com.resonix.media.feature.settings.screens.audio

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.resonix.media.core.data.repository.PreferencesRepository
import com.resonix.media.core.model.PlayerPreferences
import com.resonix.media.core.ui.base.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class AudioPreferencesViewModel(
    private val preferencesRepository: PreferencesRepository,
    @InjectedParam internal var output: Output,
) : MviViewModel<AudioPreferencesUiState, AudioPreferencesUiEvent>() {

    data class Output(
        val navigateUp: () -> Unit,
    )

    private val stateInternal = MutableStateFlow(
        AudioPreferencesUiState(
            preferences = preferencesRepository.playerPreferences.value,
        ),
    )
    override val state: StateFlow<AudioPreferencesUiState> = stateInternal.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.playerPreferences.collect { preferences ->
                stateInternal.update { currentState ->
                    currentState.copy(preferences = preferences)
                }
            }
        }
    }

    override fun onAction(action: AudioPreferencesUiEvent) {
        when (action) {
            is AudioPreferencesUiEvent.NavigateUp -> output.navigateUp()

            is AudioPreferencesUiEvent.ShowDialog -> showDialog(action.value)
            is AudioPreferencesUiEvent.UpdateAudioLanguage -> updateAudioLanguage(action.value)
            is AudioPreferencesUiEvent.TogglePauseOnHeadsetDisconnect -> togglePauseOnHeadsetDisconnect()
            is AudioPreferencesUiEvent.ToggleShowSystemVolumePanel -> toggleShowSystemVolumePanel()
            is AudioPreferencesUiEvent.ToggleRequireAudioFocus -> toggleRequireAudioFocus()
            is AudioPreferencesUiEvent.ToggleVolumeBoost -> toggleVolumeBoost()
            is AudioPreferencesUiEvent.ToggleEqualizer -> toggleEqualizer()
            is AudioPreferencesUiEvent.SelectEqualizerPreset -> selectEqualizerPreset(action.value)
            is AudioPreferencesUiEvent.UpdateEqualizerBand -> updateEqualizerBand(action.index, action.value)
            is AudioPreferencesUiEvent.UpdateEqualizerPreamp -> updateEqualizerPreamp(action.value)
        }
    }

    private fun showDialog(value: AudioPreferenceDialog?) {
        stateInternal.update {
            it.copy(showDialog = value)
        }
    }

    private fun updateAudioLanguage(value: String) {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(preferredAudioLanguage = value)
            }
        }
    }

    private fun togglePauseOnHeadsetDisconnect() {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(pauseOnHeadsetDisconnect = !it.pauseOnHeadsetDisconnect)
            }
        }
    }

    private fun toggleShowSystemVolumePanel() {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(showSystemVolumePanel = !it.showSystemVolumePanel)
            }
        }
    }

    private fun toggleRequireAudioFocus() {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(requireAudioFocus = !it.requireAudioFocus)
            }
        }
    }

    private fun toggleVolumeBoost() {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(enableVolumeBoost = !it.enableVolumeBoost)
            }
        }
    }

    private fun toggleEqualizer() {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(equalizerEnabled = !it.equalizerEnabled)
            }
        }
    }

    private fun selectEqualizerPreset(preset: EqualizerPreset) {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(equalizerBands = preset.levelsMillibels)
            }
        }
        showDialog(null)
    }

    private fun updateEqualizerBand(index: Int, decibels: Float) {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences { preferences ->
                val bands = preferences.equalizerBands
                    .ifEmpty { PlayerPreferences.DEFAULT_EQUALIZER_BANDS }
                    .toMutableList()
                if (index !in bands.indices) return@updatePlayerPreferences preferences
                bands[index] = EqualizerPreset.fromDecibels(listOf(decibels)).first()
                preferences.copy(equalizerBands = bands)
            }
        }
    }

    private fun updateEqualizerPreamp(decibels: Float) {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(equalizerGain = EqualizerPreset.fromDecibels(listOf(decibels)).first())
            }
        }
    }
}

@Stable
data class AudioPreferencesUiState(
    val showDialog: AudioPreferenceDialog? = null,
    val preferences: PlayerPreferences = PlayerPreferences(),
) {
    /** Always [PlayerPreferences.EQUALIZER_BAND_COUNT] bands, whatever the stored list holds. */
    val bands: List<Int>
        get() = preferences.equalizerBands
            .ifEmpty { PlayerPreferences.DEFAULT_EQUALIZER_BANDS }
            .take(PlayerPreferences.EQUALIZER_BAND_COUNT)
            .let { it + List(PlayerPreferences.EQUALIZER_BAND_COUNT - it.size) { 0 } }

    val preamp: Int
        get() = preferences.equalizerGain
}

/** Band levels are stored in millibels and shown in decibels. */
fun Int.toDecibels(): Float = this / MILLIBELS_PER_DECIBEL

private const val MILLIBELS_PER_DECIBEL = 100f

sealed interface AudioPreferenceDialog {
    data object AudioLanguageDialog : AudioPreferenceDialog
    data object EqualizerPresetDialog : AudioPreferenceDialog
}

sealed interface AudioPreferencesUiEvent {
    data object NavigateUp : AudioPreferencesUiEvent

    data class ShowDialog(val value: AudioPreferenceDialog?) : AudioPreferencesUiEvent
    data class UpdateAudioLanguage(val value: String) : AudioPreferencesUiEvent
    data object TogglePauseOnHeadsetDisconnect : AudioPreferencesUiEvent
    data object ToggleShowSystemVolumePanel : AudioPreferencesUiEvent
    data object ToggleRequireAudioFocus : AudioPreferencesUiEvent
    data object ToggleVolumeBoost : AudioPreferencesUiEvent
    data object ToggleEqualizer : AudioPreferencesUiEvent
    data class SelectEqualizerPreset(val value: EqualizerPreset) : AudioPreferencesUiEvent
    data class UpdateEqualizerBand(val index: Int, val value: Float) : AudioPreferencesUiEvent
    data class UpdateEqualizerPreamp(val value: Float) : AudioPreferencesUiEvent
}
