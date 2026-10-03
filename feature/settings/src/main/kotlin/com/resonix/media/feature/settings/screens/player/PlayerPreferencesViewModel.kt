package com.resonix.media.feature.settings.screens.player

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.resonix.media.core.common.extensions.round
import com.resonix.media.core.data.repository.PreferencesRepository
import com.resonix.media.core.model.ControlButtonsPosition
import com.resonix.media.core.model.PlayerPreferences
import com.resonix.media.core.model.Resume
import com.resonix.media.core.model.ScreenOrientation
import com.resonix.media.core.ui.base.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class PlayerPreferencesViewModel(
    private val preferencesRepository: PreferencesRepository,
    @InjectedParam internal var output: Output,
) : MviViewModel<PlayerPreferencesUiState, PlayerPreferencesUiEvent>() {

    data class Output(
        val navigateUp: () -> Unit,
    )

    private val stateInternal = MutableStateFlow(
        PlayerPreferencesUiState(
            preferences = preferencesRepository.playerPreferences.value,
        ),
    )
    override val state: StateFlow<PlayerPreferencesUiState> = stateInternal.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.playerPreferences.collect { preferences ->
                stateInternal.update { it.copy(preferences = preferences) }
            }
        }
    }

    override fun onAction(action: PlayerPreferencesUiEvent) {
        when (action) {
            is PlayerPreferencesUiEvent.NavigateUp -> output.navigateUp()

            is PlayerPreferencesUiEvent.ShowDialog -> showDialog(action.value)
            is PlayerPreferencesUiEvent.UpdatePlaybackResume -> updatePlaybackResume(action.resume)
            is PlayerPreferencesUiEvent.ToggleAutoplay -> toggleAutoplay()
            is PlayerPreferencesUiEvent.ToggleAutoPip -> toggleAutoPip()
            is PlayerPreferencesUiEvent.ToggleAutoBackgroundPlay -> toggleAutoBackgroundPlay()
            is PlayerPreferencesUiEvent.ToggleRememberBrightnessLevel -> toggleRememberBrightnessLevel()
            is PlayerPreferencesUiEvent.ToggleRememberSelections -> toggleRememberSelections()
            is PlayerPreferencesUiEvent.UpdatePreferredPlayerOrientation -> updatePreferredPlayerOrientation(action.value)
            is PlayerPreferencesUiEvent.UpdatePreferredControlButtonsPosition -> updatePreferredControlButtonsPosition(action.value)
            is PlayerPreferencesUiEvent.UpdateDefaultPlaybackSpeed -> updateDefaultPlaybackSpeed(action.value)
            is PlayerPreferencesUiEvent.UpdateControlAutoHideTimeout -> updateControlAutoHideTimeout(action.value)
            is PlayerPreferencesUiEvent.UpdateVideoContrast -> updateVideoEffect { it.copy(videoContrast = action.value) }
            is PlayerPreferencesUiEvent.UpdateVideoSaturation -> updateVideoEffect { it.copy(videoSaturation = action.value) }
            is PlayerPreferencesUiEvent.UpdateVideoHue -> updateVideoEffect { it.copy(videoHue = action.value) }
            is PlayerPreferencesUiEvent.ToggleUseMaterialYouControls -> toggleUseMaterialYouControls()
        }
    }

    private fun showDialog(value: PlayerPreferenceDialog?) {
        stateInternal.update {
            it.copy(showDialog = value)
        }
    }

    private fun updatePlaybackResume(resume: Resume) {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(
                    resume = resume,
                )
            }
        }
    }

    private fun toggleAutoplay() {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(autoplay = !it.autoplay)
            }
        }
    }

    private fun toggleAutoPip() {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(autoPip = !it.autoPip)
            }
        }
    }

    private fun toggleAutoBackgroundPlay() {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(autoBackgroundPlay = !it.autoBackgroundPlay)
            }
        }
    }

    private fun toggleRememberBrightnessLevel() {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(rememberPlayerBrightness = !it.rememberPlayerBrightness)
            }
        }
    }

    private fun toggleRememberSelections() {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(rememberSelections = !it.rememberSelections)
            }
        }
    }

    private fun updatePreferredPlayerOrientation(value: ScreenOrientation) {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(playerScreenOrientation = value)
            }
        }
    }

    private fun updatePreferredControlButtonsPosition(value: ControlButtonsPosition) {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(controlButtonsPosition = value)
            }
        }
    }

    private fun updateDefaultPlaybackSpeed(value: Float) {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(defaultPlaybackSpeed = value.round(1))
            }
        }
    }

    private fun updateControlAutoHideTimeout(value: Int) {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(controllerAutoHideTimeout = value)
            }
        }
    }

    private fun updateVideoEffect(transform: (PlayerPreferences) -> PlayerPreferences) {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences(transform)
        }
    }

    private fun toggleUseMaterialYouControls() {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(useMaterialYouControls = !it.useMaterialYouControls)
            }
        }
    }
}

@Stable
data class PlayerPreferencesUiState(
    val showDialog: PlayerPreferenceDialog? = null,
    val preferences: PlayerPreferences = PlayerPreferences(),
)

sealed interface PlayerPreferenceDialog {
    data object ResumeDialog : PlayerPreferenceDialog
    data object PlayerScreenOrientationDialog : PlayerPreferenceDialog
    data object ControlButtonsDialog : PlayerPreferenceDialog
}

sealed interface PlayerPreferencesUiEvent {
    data object NavigateUp : PlayerPreferencesUiEvent

    data class ShowDialog(val value: PlayerPreferenceDialog?) : PlayerPreferencesUiEvent
    data class UpdatePlaybackResume(val resume: Resume) : PlayerPreferencesUiEvent
    data object ToggleAutoplay : PlayerPreferencesUiEvent
    data object ToggleAutoPip : PlayerPreferencesUiEvent
    data object ToggleAutoBackgroundPlay : PlayerPreferencesUiEvent
    data object ToggleRememberBrightnessLevel : PlayerPreferencesUiEvent
    data object ToggleRememberSelections : PlayerPreferencesUiEvent
    data class UpdatePreferredPlayerOrientation(val value: ScreenOrientation) : PlayerPreferencesUiEvent
    data class UpdatePreferredControlButtonsPosition(val value: ControlButtonsPosition) : PlayerPreferencesUiEvent
    data class UpdateDefaultPlaybackSpeed(val value: Float) : PlayerPreferencesUiEvent
    data class UpdateControlAutoHideTimeout(val value: Int) : PlayerPreferencesUiEvent
    data object ToggleUseMaterialYouControls : PlayerPreferencesUiEvent
    data class UpdateVideoContrast(val value: Float) : PlayerPreferencesUiEvent
    data class UpdateVideoSaturation(val value: Float) : PlayerPreferencesUiEvent
    data class UpdateVideoHue(val value: Float) : PlayerPreferencesUiEvent
}
