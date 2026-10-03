package com.resonix.media.feature.settings.screens.appearance

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resonix.media.core.model.ThemeConfig
import com.resonix.media.core.ui.R
import com.resonix.media.core.ui.components.ListSectionTitle
import com.resonix.media.core.ui.components.ResonixTopAppBar
import com.resonix.media.core.ui.components.ClickablePreferenceItem
import com.resonix.media.core.ui.components.PreferenceSwitch
import com.resonix.media.core.ui.components.PreferenceSwitchWithDivider
import com.resonix.media.core.ui.components.RadioTextButton
import com.resonix.media.core.ui.components.rememberTvListFocusRequester
import com.resonix.media.core.ui.components.tvFocusDown
import com.resonix.media.core.ui.components.tvListFocus
import com.resonix.media.core.ui.designsystem.ResonixIcons
import com.resonix.media.core.ui.theme.ResonixTheme
import com.resonix.media.core.ui.theme.supportsDynamicTheming
import com.resonix.media.feature.settings.composables.OptionsDialog
import com.resonix.media.feature.settings.extensions.name

private data class AccentPreset(val name: String, val argb: Long)

private val accentPresets = listOf(
    AccentPreset("Blue", 0xFF1B6EF3),
    AccentPreset("Teal", 0xFF00897B),
    AccentPreset("Green", 0xFF2E7D32),
    AccentPreset("Amber", 0xFFF9A825),
    AccentPreset("Red", 0xFFD32F2F),
    AccentPreset("Purple", 0xFF7B1FA2),
    AccentPreset("Pink", 0xFFC2185B),
    AccentPreset("Graphite", 0xFF424242),
)

@Composable
fun AppearancePreferencesScreen(
    viewModel: AppearancePreferencesViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AppearancePreferencesScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppearancePreferencesScreenContent(
    state: AppearancePreferencesUiState,
    onAction: (AppearancePreferencesEvent) -> Unit,
) {
    val listFocusRequester = rememberTvListFocusRequester()
    Scaffold(
        topBar = {
            ResonixTopAppBar(
                title = stringResource(id = R.string.appearance_name),
                navigationIcon = {
                    FilledTonalIconButton(onClick = { onAction(AppearancePreferencesEvent.NavigateUp) }, modifier = Modifier.tvFocusDown(listFocusRequester)) {
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
            ListSectionTitle(text = stringResource(id = R.string.appearance_name))
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                PreferenceSwitchWithDivider(
                    title = stringResource(id = R.string.dark_theme),
                    description = state.preferences.themeConfig.name(),
                    isChecked = state.preferences.themeConfig == ThemeConfig.ON,
                    onChecked = { onAction(AppearancePreferencesEvent.ToggleDarkTheme) },
                    icon = ResonixIcons.DarkMode,
                    onClick = { onAction(AppearancePreferencesEvent.ShowDialog(AppearancePreferenceDialog.Theme)) },
                    isFirstItem = true,
                )
                PreferenceSwitch(
                    title = stringResource(R.string.high_contrast_dark_theme),
                    description = stringResource(R.string.high_contrast_dark_theme_desc),
                    icon = ResonixIcons.Contrast,
                    isChecked = state.preferences.useHighContrastDarkTheme,
                    onClick = { onAction(AppearancePreferencesEvent.ToggleUseHighContrastDarkTheme) },
                    isLastItem = !supportsDynamicTheming(),
                )
                if (supportsDynamicTheming()) {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.dynamic_theme),
                        description = stringResource(id = R.string.dynamic_theme_description),
                        icon = ResonixIcons.Appearance,
                        isChecked = state.preferences.useDynamicColors,
                        onClick = { onAction(AppearancePreferencesEvent.ToggleUseDynamicColors) },
                    )
                }
                ClickablePreferenceItem(
                    title = stringResource(R.string.accent_color),
                    description = stringResource(R.string.accent_color_desc),
                    icon = ResonixIcons.Appearance,
                    onClick = { onAction(AppearancePreferencesEvent.ShowDialog(AppearancePreferenceDialog.AccentColor)) },
                    isLastItem = true,
                )
            }
        }

        state.showDialog?.let { showDialog ->
            when (showDialog) {
                AppearancePreferenceDialog.Theme -> {
                    OptionsDialog(
                        text = stringResource(id = R.string.dark_theme),
                        onDismissClick = { onAction(AppearancePreferencesEvent.ShowDialog(null)) },
                    ) {
                        items(ThemeConfig.entries.toTypedArray()) {
                            RadioTextButton(
                                text = it.name(),
                                selected = (it == state.preferences.themeConfig),
                                onClick = {
                                    onAction(AppearancePreferencesEvent.UpdateThemeConfig(it))
                                    onAction(AppearancePreferencesEvent.ShowDialog(null))
                                },
                            )
                        }
                    }
                }

                AppearancePreferenceDialog.AccentColor -> {
                    OptionsDialog(
                        text = stringResource(R.string.accent_color),
                        onDismissClick = { onAction(AppearancePreferencesEvent.ShowDialog(null)) },
                    ) {
                        item {
                            RadioTextButton(
                                text = stringResource(R.string.accent_default),
                                selected = state.preferences.accentColor == null,
                                onClick = {
                                    onAction(AppearancePreferencesEvent.UpdateAccentColor(null))
                                    onAction(AppearancePreferencesEvent.ShowDialog(null))
                                },
                            )
                        }
                        items(accentPresets) {
                            RadioTextButton(
                                text = it.name,
                                selected = state.preferences.accentColor == it.argb,
                                onClick = {
                                    onAction(AppearancePreferencesEvent.UpdateAccentColor(it.argb))
                                    onAction(AppearancePreferencesEvent.ShowDialog(null))
                                },
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
private fun AppearancePreferencesScreenPreview() {
    ResonixTheme {
        AppearancePreferencesScreenContent(
            state = AppearancePreferencesUiState(),
            onAction = {},
        )
    }
}
