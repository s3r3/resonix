package com.resonix.media.feature.settings.screens.audio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resonix.media.core.model.PlayerPreferences
import com.resonix.media.core.ui.R
import com.resonix.media.core.ui.components.ClickablePreferenceItem
import com.resonix.media.core.ui.components.ListSectionTitle
import com.resonix.media.core.ui.components.NextTopAppBar
import com.resonix.media.core.ui.components.PreferenceSlider
import com.resonix.media.core.ui.components.PreferenceSwitch
import com.resonix.media.core.ui.components.RadioTextButton
import com.resonix.media.core.ui.components.rememberTvListFocusRequester
import com.resonix.media.core.ui.components.tvFocusDown
import com.resonix.media.core.ui.components.tvListFocus
import com.resonix.media.core.ui.designsystem.NextIcons
import com.resonix.media.core.ui.theme.ResonixTheme
import com.resonix.media.feature.settings.composables.OptionsDialog
import com.resonix.media.feature.settings.utils.LocalesHelper

@Composable
fun AudioPreferencesScreen(
    viewModel: AudioPreferencesViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AudioPreferencesScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AudioPreferencesScreenContent(
    state: AudioPreferencesUiState,
    onAction: (AudioPreferencesUiEvent) -> Unit,
) {
    val languages = remember { listOf(Pair("None", "")) + LocalesHelper.getAvailableLocales() }

    val listFocusRequester = rememberTvListFocusRequester()
    Scaffold(
        topBar = {
            NextTopAppBar(
                title = stringResource(id = R.string.audio),
                navigationIcon = {
                    FilledTonalIconButton(onClick = { onAction(AudioPreferencesUiEvent.NavigateUp) }, modifier = Modifier.tvFocusDown(listFocusRequester)) {
                        Icon(
                            imageVector = NextIcons.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_up),
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .tvListFocus(listFocusRequester)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            ListSectionTitle(text = stringResource(id = R.string.playback))
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                ClickablePreferenceItem(
                    title = stringResource(id = R.string.preferred_audio_lang),
                    description = LocalesHelper.getLocaleDisplayLanguage(state.preferences.preferredAudioLanguage)
                        .takeIf { it.isNotBlank() } ?: stringResource(R.string.preferred_audio_lang_description),
                    icon = NextIcons.Language,
                    onClick = { onAction(AudioPreferencesUiEvent.ShowDialog(AudioPreferenceDialog.AudioLanguageDialog)) },
                    isFirstItem = true,
                )
                PreferenceSwitch(
                    title = stringResource(R.string.require_audio_focus),
                    description = stringResource(R.string.require_audio_focus_desc),
                    icon = NextIcons.Focus,
                    isChecked = state.preferences.requireAudioFocus,
                    onClick = { onAction(AudioPreferencesUiEvent.ToggleRequireAudioFocus) },
                )
                PreferenceSwitch(
                    title = stringResource(id = R.string.pause_on_headset_disconnect),
                    description = stringResource(id = R.string.pause_on_headset_disconnect_desc),
                    icon = NextIcons.HeadsetOff,
                    isChecked = state.preferences.pauseOnHeadsetDisconnect,
                    onClick = { onAction(AudioPreferencesUiEvent.TogglePauseOnHeadsetDisconnect) },
                )
                PreferenceSwitch(
                    title = stringResource(id = R.string.system_volume_panel),
                    description = stringResource(id = R.string.system_volume_panel_desc),
                    icon = NextIcons.Headset,
                    isChecked = state.preferences.showSystemVolumePanel,
                    onClick = { onAction(AudioPreferencesUiEvent.ToggleShowSystemVolumePanel) },
                )
PreferenceSwitch(
                    title = stringResource(R.string.volume_boost),
                    description = stringResource(R.string.volume_boost_desc),
                    icon = NextIcons.VolumeUp,
                    isChecked = state.preferences.enableVolumeBoost,
                    onClick = { onAction(AudioPreferencesUiEvent.ToggleVolumeBoost) },
                    isLastItem = true,
                )
            }

            ListSectionTitle(text = stringResource(R.string.equalizer))
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                PreferenceSwitch(
                    title = stringResource(R.string.equalizer),
                    description = stringResource(R.string.equalizer_desc),
                    icon = NextIcons.Equalizer,
                    isChecked = state.preferences.equalizerEnabled,
                    onClick = { onAction(AudioPreferencesUiEvent.ToggleEqualizer) },
                    isFirstItem = true,
                )
                ClickablePreferenceItem(
                    title = stringResource(R.string.equalizer_preset),
                    description = stringResource(
                        EqualizerPreset.match(state.bands).label,
                    ),
                    icon = NextIcons.EqualizerPreset,
                    enabled = state.preferences.equalizerEnabled,
                    onClick = { onAction(AudioPreferencesUiEvent.ShowDialog(AudioPreferenceDialog.EqualizerPresetDialog)) },
                )
                state.bands.forEachIndexed { index, level ->
                    PreferenceSlider(
                        title = stringResource(equalizerBandLabels[index]),
                        description = stringResource(R.string.equalizer_decibels, level.toDecibels()),
                        icon = NextIcons.Equalizer,
                        enabled = state.preferences.equalizerEnabled,
                        value = level.toDecibels(),
                        valueRange = PlayerPreferences.EQUALIZER_MIN_LEVEL.toDecibels()..
                            PlayerPreferences.EQUALIZER_MAX_LEVEL.toDecibels(),
                        onValueChange = { onAction(AudioPreferencesUiEvent.UpdateEqualizerBand(index, it)) },
                        onReset = {
                            onAction(AudioPreferencesUiEvent.UpdateEqualizerBand(index, 0f))
                        },
                        isLastItem = index == state.bands.lastIndex,
                    )
                }
                PreferenceSlider(
                    title = stringResource(R.string.equalizer_preamp),
                    description = stringResource(R.string.equalizer_decibels, state.preamp.toDecibels()),
                    icon = NextIcons.VolumeUp,
                    enabled = state.preferences.equalizerEnabled,
                    value = state.preamp.toDecibels(),
                    valueRange = PlayerPreferences.EQUALIZER_MIN_LEVEL.toDecibels()..
                        PlayerPreferences.EQUALIZER_MAX_LEVEL.toDecibels(),
                    onValueChange = { onAction(AudioPreferencesUiEvent.UpdateEqualizerPreamp(it)) },
                    onReset = { onAction(AudioPreferencesUiEvent.UpdateEqualizerPreamp(0f)) },
                    isLastItem = true,
                )
            }
        }

        state.showDialog?.let { showDialog ->
            when (showDialog) {
                AudioPreferenceDialog.AudioLanguageDialog -> {
                    OptionsDialog(
                        text = stringResource(id = R.string.preferred_audio_lang),
                        onDismissClick = { onAction(AudioPreferencesUiEvent.ShowDialog(null)) },
                    ) {
                        items(languages) {
                            RadioTextButton(
                                text = it.first,
                                selected = it.second == state.preferences.preferredAudioLanguage,
                                onClick = {
                                    onAction(AudioPreferencesUiEvent.UpdateAudioLanguage(it.second))
                                    onAction(AudioPreferencesUiEvent.ShowDialog(null))
                                },
                            )
                        }
                    }
                }

                AudioPreferenceDialog.EqualizerPresetDialog -> {
                    OptionsDialog(
                        text = stringResource(R.string.equalizer_preset),
                        onDismissClick = { onAction(AudioPreferencesUiEvent.ShowDialog(null)) },
                    ) {
                        items(EqualizerPreset.entries) { preset ->
                            RadioTextButton(
                                text = stringResource(preset.label),
                                selected = preset == EqualizerPreset.match(state.bands),
                                onClick = { onAction(AudioPreferencesUiEvent.SelectEqualizerPreset(preset)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AudioPreferencesScreenPreview() {
    ResonixTheme {
        AudioPreferencesScreenContent(
            state = AudioPreferencesUiState(),
            onAction = {},
        )
    }
}
