package com.resonix.media.feature.settings.screens.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resonix.media.core.common.extensions.isPipFeatureSupported
import com.resonix.media.core.model.ControlButtonsPosition
import com.resonix.media.core.model.PlayerPreferences
import com.resonix.media.core.model.Resume
import com.resonix.media.core.model.ScreenOrientation
import com.resonix.media.core.ui.R
import com.resonix.media.core.ui.components.ClickablePreferenceItem
import com.resonix.media.core.ui.components.ListSectionTitle
import com.resonix.media.core.ui.components.ResonixTopAppBar
import com.resonix.media.core.ui.components.PreferenceSlider
import com.resonix.media.core.ui.components.PreferenceSwitch
import com.resonix.media.core.ui.components.RadioTextButton
import com.resonix.media.core.ui.components.rememberTvListFocusRequester
import com.resonix.media.core.ui.components.tvFocusDown
import com.resonix.media.core.ui.components.tvListFocus
import com.resonix.media.core.ui.designsystem.ResonixIcons
import com.resonix.media.core.ui.preview.DayNightPreview
import com.resonix.media.core.ui.theme.ResonixTheme
import com.resonix.media.feature.settings.composables.OptionsDialog
import com.resonix.media.feature.settings.extensions.name

@Composable
fun PlayerPreferencesScreen(
    viewModel: PlayerPreferencesViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PlayerPreferencesScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PlayerPreferencesScreenContent(
    state: PlayerPreferencesUiState,
    onAction: (PlayerPreferencesUiEvent) -> Unit,
) {
    val listFocusRequester = rememberTvListFocusRequester()
    Scaffold(
        topBar = {
            ResonixTopAppBar(
                title = stringResource(id = R.string.player_name),
                navigationIcon = {
                    FilledTonalIconButton(onClick = { onAction(PlayerPreferencesUiEvent.NavigateUp) }, modifier = Modifier.tvFocusDown(listFocusRequester)) {
                        Icon(
                            imageVector = ResonixIcons.ArrowBack,
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
            ListSectionTitle(text = stringResource(id = R.string.interface_name))
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                PreferenceSwitch(
                    title = stringResource(id = R.string.material_you_controls),
                    description = stringResource(id = R.string.material_you_controls_description),
                    icon = ResonixIcons.Appearance,
                    isChecked = state.preferences.useMaterialYouControls,
                    onClick = { onAction(PlayerPreferencesUiEvent.ToggleUseMaterialYouControls) },
                    isFirstItem = true,
                )
                PreferenceSlider(
                    title = stringResource(R.string.controller_timeout),
                    description = stringResource(R.string.seconds, state.preferences.controllerAutoHideTimeout),
                    icon = ResonixIcons.Timer,
                    value = state.preferences.controllerAutoHideTimeout.toFloat(),
                    valueRange = 1.0f..60.0f,
                    onValueChange = { onAction(PlayerPreferencesUiEvent.UpdateControlAutoHideTimeout(it.toInt())) },
                    onReset = { onAction(PlayerPreferencesUiEvent.UpdateControlAutoHideTimeout(PlayerPreferences.DEFAULT_CONTROLLER_AUTO_HIDE_TIMEOUT)) },
                    isLastItem = true,
                    trailingContent = {
                        FilledIconButton(onClick = { onAction(PlayerPreferencesUiEvent.UpdateControlAutoHideTimeout(PlayerPreferences.DEFAULT_CONTROLLER_AUTO_HIDE_TIMEOUT)) }) {
                            Icon(
                                imageVector = ResonixIcons.History,
                                contentDescription = stringResource(id = R.string.reset_controller_timeout),
                            )
                        }
                    },
                )
            }

            ListSectionTitle(text = stringResource(id = R.string.playback))
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                ClickablePreferenceItem(
                    title = stringResource(id = R.string.resume),
                    description = stringResource(id = R.string.resume_description),
                    icon = ResonixIcons.Resume,
                    onClick = { onAction(PlayerPreferencesUiEvent.ShowDialog(PlayerPreferenceDialog.ResumeDialog)) },
                    isFirstItem = true,
                )
                PreferenceSlider(
                    title = stringResource(id = R.string.default_playback_speed),
                    description = state.preferences.defaultPlaybackSpeed.toString(),
                    icon = ResonixIcons.Speed,
                    value = state.preferences.defaultPlaybackSpeed,
                    valueRange = 0.2f..4.0f,
                    onValueChange = { onAction(PlayerPreferencesUiEvent.UpdateDefaultPlaybackSpeed(it)) },
                    onReset = { onAction(PlayerPreferencesUiEvent.UpdateDefaultPlaybackSpeed(1f)) },
                    trailingContent = {
                        FilledIconButton(onClick = { onAction(PlayerPreferencesUiEvent.UpdateDefaultPlaybackSpeed(1f)) }) {
                            Icon(
                                imageVector = ResonixIcons.History,
                                contentDescription = stringResource(id = R.string.reset_default_playback_speed),
                            )
                        }
                    },
                )
                PreferenceSwitch(
                    title = stringResource(id = R.string.autoplay_settings),
                    description = stringResource(
                        id = R.string.autoplay_settings_description,
                    ),
                    icon = ResonixIcons.Player,
                    isChecked = state.preferences.autoplay,
                    onClick = { onAction(PlayerPreferencesUiEvent.ToggleAutoplay) },
                )
                if (LocalContext.current.isPipFeatureSupported) {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.pip_settings),
                        description = stringResource(
                            id = R.string.pip_settings_description,
                        ),
                        icon = ResonixIcons.Pip,
                        isChecked = state.preferences.autoPip,
                        onClick = { onAction(PlayerPreferencesUiEvent.ToggleAutoPip) },
                    )
                }
                PreferenceSwitch(
                    title = stringResource(id = R.string.background_play),
                    description = stringResource(
                        id = R.string.background_play_description,
                    ),
                    icon = ResonixIcons.Headset,
                    isChecked = state.preferences.autoBackgroundPlay,
                    onClick = { onAction(PlayerPreferencesUiEvent.ToggleAutoBackgroundPlay) },
                )
                PreferenceSwitch(
                    title = stringResource(id = R.string.remember_brightness_level),
                    description = stringResource(
                        id = R.string.remember_brightness_level_description,
                    ),
                    icon = ResonixIcons.Brightness,
                    isChecked = state.preferences.rememberPlayerBrightness,
                    onClick = { onAction(PlayerPreferencesUiEvent.ToggleRememberBrightnessLevel) },
                )
                PreferenceSwitch(
                    title = stringResource(id = R.string.remember_selections),
                    description = stringResource(id = R.string.remember_selections_description),
                    icon = ResonixIcons.Selection,
                    isChecked = state.preferences.rememberSelections,
                    onClick = { onAction(PlayerPreferencesUiEvent.ToggleRememberSelections) },
                )
                ClickablePreferenceItem(
                    title = stringResource(id = R.string.player_screen_orientation),
                    description = state.preferences.playerScreenOrientation.name(),
                    icon = ResonixIcons.Rotation,
                    onClick = {
                        onAction(PlayerPreferencesUiEvent.ShowDialog(PlayerPreferenceDialog.PlayerScreenOrientationDialog))
                    },
                    isLastItem = true,
                )
            }

            ListSectionTitle(text = stringResource(id = R.string.video_effects))
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                PreferenceSlider(
                    title = stringResource(id = R.string.video_contrast),
                    description = stringResource(id = R.string.multiplier, state.preferences.videoContrast),
                    icon = ResonixIcons.Contrast,
                    value = state.preferences.videoContrast,
                    valueRange = 0f..PlayerPreferences.VIDEO_CONTRAST_RANGE,
                    onValueChange = { onAction(PlayerPreferencesUiEvent.UpdateVideoContrast(it)) },
                    onReset = { onAction(PlayerPreferencesUiEvent.UpdateVideoContrast(1f)) },
                    isFirstItem = true,
                )
                PreferenceSlider(
                    title = stringResource(id = R.string.video_saturation),
                    description = stringResource(id = R.string.multiplier, state.preferences.videoSaturation),
                    icon = ResonixIcons.Color,
                    value = state.preferences.videoSaturation,
                    valueRange = 0f..PlayerPreferences.VIDEO_SATURATION_RANGE,
                    onValueChange = { onAction(PlayerPreferencesUiEvent.UpdateVideoSaturation(it)) },
                    onReset = { onAction(PlayerPreferencesUiEvent.UpdateVideoSaturation(1f)) },
                )
                PreferenceSlider(
                    title = stringResource(id = R.string.video_hue),
                    description = stringResource(id = R.string.degrees, state.preferences.videoHue),
                    icon = ResonixIcons.Appearance,
                    value = state.preferences.videoHue,
                    valueRange = -PlayerPreferences.VIDEO_HUE_RANGE..PlayerPreferences.VIDEO_HUE_RANGE,
                    onValueChange = { onAction(PlayerPreferencesUiEvent.UpdateVideoHue(it)) },
                    onReset = { onAction(PlayerPreferencesUiEvent.UpdateVideoHue(0f)) },
                    isLastItem = true,
                )
            }
        }

        state.showDialog?.let { showDialog ->
            when (showDialog) {
                PlayerPreferenceDialog.ResumeDialog -> {
                    OptionsDialog(
                        text = stringResource(id = R.string.resume),
                        onDismissClick = { onAction(PlayerPreferencesUiEvent.ShowDialog(null)) },
                    ) {
                        items(Resume.entries.toTypedArray()) {
                            RadioTextButton(
                                text = it.name(),
                                selected = (it == state.preferences.resume),
                                onClick = {
                                    onAction(PlayerPreferencesUiEvent.UpdatePlaybackResume(it))
                                    onAction(PlayerPreferencesUiEvent.ShowDialog(null))
                                },
                            )
                        }
                    }
                }

                PlayerPreferenceDialog.PlayerScreenOrientationDialog -> {
                    OptionsDialog(
                        text = stringResource(id = R.string.player_screen_orientation),
                        onDismissClick = { onAction(PlayerPreferencesUiEvent.ShowDialog(null)) },
                    ) {
                        items(ScreenOrientation.entries.toTypedArray()) {
                            RadioTextButton(
                                text = it.name(),
                                selected = it == state.preferences.playerScreenOrientation,
                                onClick = {
                                    onAction(PlayerPreferencesUiEvent.UpdatePreferredPlayerOrientation(it))
                                    onAction(PlayerPreferencesUiEvent.ShowDialog(null))
                                },
                            )
                        }
                    }
                }

                PlayerPreferenceDialog.ControlButtonsDialog -> {
                    OptionsDialog(
                        text = stringResource(id = R.string.control_buttons_alignment),
                        onDismissClick = { onAction(PlayerPreferencesUiEvent.ShowDialog(null)) },
                    ) {
                        items(ControlButtonsPosition.entries.toTypedArray()) {
                            RadioTextButton(
                                text = it.name(),
                                selected = it == state.preferences.controlButtonsPosition,
                                onClick = {
                                    onAction(PlayerPreferencesUiEvent.UpdatePreferredControlButtonsPosition(it))
                                    onAction(PlayerPreferencesUiEvent.ShowDialog(null))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@DayNightPreview
@Composable
private fun PlayerPreferencesScreenPreview() {
    ResonixTheme {
        PlayerPreferencesScreenContent(
            state = PlayerPreferencesUiState(),
            onAction = {},
        )
    }
}
