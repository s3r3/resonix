package com.resonix.media.feature.settings.screens.subtitle

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resonix.media.core.model.Font
import com.resonix.media.core.model.PlayerPreferences
import com.resonix.media.core.ui.R
import com.resonix.media.core.ui.components.ClickablePreferenceItem
import com.resonix.media.core.ui.components.ListSectionTitle
import com.resonix.media.core.ui.components.NextOutlinedTextField
import com.resonix.media.core.ui.components.NextTopAppBar
import com.resonix.media.core.ui.components.PreferenceSlider
import com.resonix.media.core.ui.components.PreferenceSwitch
import com.resonix.media.core.ui.components.PreferenceSwitchWithDivider
import com.resonix.media.core.ui.components.RadioTextButton
import com.resonix.media.core.ui.components.rememberTvListFocusRequester
import com.resonix.media.core.ui.components.tvFocusDown
import com.resonix.media.core.ui.components.tvListFocus
import com.resonix.media.core.ui.designsystem.NextIcons
import com.resonix.media.core.ui.theme.ResonixTheme
import com.resonix.media.feature.settings.composables.OptionsDialog
import com.resonix.media.feature.settings.extensions.name
import com.resonix.media.feature.settings.utils.LocalesHelper
import java.nio.charset.Charset

@Composable
fun SubtitlePreferencesScreen(
    viewModel: SubtitlePreferencesViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SubtitlePreferencesScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SubtitlePreferencesScreenContent(
    state: SubtitlePreferencesUiState,
    onAction: (SubtitlePreferencesUiEvent) -> Unit,
) {
    val languages = remember { listOf(Pair("None", "")) + LocalesHelper.getAvailableLocales() }
    val charsetResource = stringArrayResource(id = R.array.charsets_list)
    val context = LocalContext.current

    val listFocusRequester = rememberTvListFocusRequester()
    Scaffold(
        topBar = {
            NextTopAppBar(
                title = stringResource(id = R.string.subtitle),
                navigationIcon = {
                    FilledTonalIconButton(onClick = { onAction(SubtitlePreferencesUiEvent.NavigateUp) }, modifier = Modifier.tvFocusDown(listFocusRequester)) {
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
                    title = stringResource(id = R.string.preferred_subtitle_lang),
                    description = LocalesHelper.getLocaleDisplayLanguage(state.preferences.preferredSubtitleLanguage)
                        .takeIf { it.isNotBlank() } ?: stringResource(R.string.preferred_subtitle_lang_description),
                    icon = NextIcons.Language,
                    onClick = { onAction(SubtitlePreferencesUiEvent.ShowDialog(SubtitlePreferenceDialog.SubtitleLanguageDialog)) },
                    isFirstItem = true,
                )
                ClickablePreferenceItem(
                    title = stringResource(R.string.subtitle_text_encoding),
                    description = charsetResource.first { it.contains(state.preferences.subtitleTextEncoding) },
                    icon = NextIcons.Subtitle,
                    onClick = { onAction(SubtitlePreferencesUiEvent.ShowDialog(SubtitlePreferenceDialog.SubtitleEncodingDialog)) },
                    isLastItem = true,
                )
            }
            ListSectionTitle(text = stringResource(id = R.string.online_subtitles))
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                ClickablePreferenceItem(
                    title = stringResource(id = R.string.opensubtitles),
                    description = if (state.preferences.openSubtitlesApiKey.isNotBlank()) {
                        stringResource(id = R.string.configured)
                    } else {
                        stringResource(id = R.string.not_configured)
                    },
                    icon = NextIcons.Subtitle,
                    onClick = {
                        onAction(SubtitlePreferencesUiEvent.ShowDialog(SubtitlePreferenceDialog.OpenSubtitlesDialog))
                    },
                    isFirstItem = true,
                    isLastItem = true,
                )
            }
            ListSectionTitle(text = stringResource(id = R.string.appearance_name))
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                PreferenceSwitchWithDivider(
                    title = stringResource(R.string.system_caption_style),
                    description = stringResource(R.string.system_caption_style_desc),
                    icon = NextIcons.Caption,
                    isChecked = state.preferences.useSystemCaptionStyle,
                    onChecked = { onAction(SubtitlePreferencesUiEvent.ToggleUseSystemCaptionStyle) },
                    onClick = { context.startActivity(Intent(Settings.ACTION_CAPTIONING_SETTINGS)) },
                    isFirstItem = true,
                )
                ClickablePreferenceItem(
                    title = stringResource(id = R.string.subtitle_font),
                    description = state.preferences.subtitleFont.name(),
                    icon = NextIcons.Font,
                    enabled = state.preferences.useSystemCaptionStyle.not(),
                    onClick = { onAction(SubtitlePreferencesUiEvent.ShowDialog(SubtitlePreferenceDialog.SubtitleFontDialog)) },
                )
                PreferenceSwitch(
                    title = stringResource(id = R.string.subtitle_text_bold),
                    description = stringResource(id = R.string.subtitle_text_bold_desc),
                    icon = NextIcons.Bold,
                    enabled = state.preferences.useSystemCaptionStyle.not(),
                    isChecked = state.preferences.subtitleTextBold,
                    onClick = { onAction(SubtitlePreferencesUiEvent.ToggleSubtitleTextBold) },
                )
                PreferenceSlider(
                    title = stringResource(id = R.string.subtitle_text_size),
                    description = state.preferences.subtitleTextSize.toString(),
                    icon = NextIcons.FontSize,
                    enabled = state.preferences.useSystemCaptionStyle.not(),
                    value = state.preferences.subtitleTextSize.toFloat(),
                    valueRange = 10f..60f,
                    onValueChange = { onAction(SubtitlePreferencesUiEvent.UpdateSubtitleFontSize(it.toInt())) },
                    trailingContent = {
                        FilledIconButton(
                            enabled = state.preferences.useSystemCaptionStyle.not(),
                            onClick = {
                                onAction(SubtitlePreferencesUiEvent.UpdateSubtitleFontSize(PlayerPreferences.DEFAULT_SUBTITLE_TEXT_SIZE))
                            },
                        ) {
                            Icon(
                                imageVector = NextIcons.History,
                                contentDescription = stringResource(id = R.string.reset_seek_increment),
                            )
                        }
                    },
                )
                PreferenceSwitch(
                    title = stringResource(id = R.string.subtitle_background),
                    description = stringResource(id = R.string.subtitle_background_desc),
                    icon = NextIcons.Background,
                    enabled = state.preferences.useSystemCaptionStyle.not(),
                    isChecked = state.preferences.subtitleBackground,
                    onClick = { onAction(SubtitlePreferencesUiEvent.ToggleSubtitleBackground) },
                )
                PreferenceSwitch(
                    title = stringResource(R.string.embedded_styles),
                    description = stringResource(R.string.embedded_styles_desc),
                    icon = NextIcons.Style,
                    isChecked = state.preferences.applyEmbeddedStyles,
                    onClick = { onAction(SubtitlePreferencesUiEvent.ToggleApplyEmbeddedStyles) },
                    isLastItem = true,
                )
            }
        }

        state.showDialog?.let { showDialog ->
            when (showDialog) {
                SubtitlePreferenceDialog.SubtitleLanguageDialog -> {
                    OptionsDialog(
                        text = stringResource(id = R.string.preferred_subtitle_lang),
                        onDismissClick = { onAction(SubtitlePreferencesUiEvent.ShowDialog(null)) },
                    ) {
                        items(languages) {
                            RadioTextButton(
                                text = it.first,
                                selected = it.second == state.preferences.preferredSubtitleLanguage,
                                onClick = {
                                    onAction(SubtitlePreferencesUiEvent.UpdateSubtitleLanguage(it.second))
                                    onAction(SubtitlePreferencesUiEvent.ShowDialog(null))
                                },
                            )
                        }
                    }
                }

                SubtitlePreferenceDialog.SubtitleFontDialog -> {
                    OptionsDialog(
                        text = stringResource(id = R.string.subtitle_font),
                        onDismissClick = { onAction(SubtitlePreferencesUiEvent.ShowDialog(null)) },
                    ) {
                        items(Font.entries.toTypedArray()) {
                            RadioTextButton(
                                text = it.name(),
                                selected = it == state.preferences.subtitleFont,
                                onClick = {
                                    onAction(SubtitlePreferencesUiEvent.UpdateSubtitleFont(it))
                                    onAction(SubtitlePreferencesUiEvent.ShowDialog(null))
                                },
                            )
                        }
                    }
                }

                SubtitlePreferenceDialog.SubtitleEncodingDialog -> {
                    OptionsDialog(
                        text = stringResource(id = R.string.subtitle_text_encoding),
                        onDismissClick = { onAction(SubtitlePreferencesUiEvent.ShowDialog(null)) },
                    ) {
                        items(charsetResource) {
                            val currentCharset = it.substringAfterLast("(", "").removeSuffix(")")
                            if (currentCharset.isEmpty() || Charset.isSupported(currentCharset)) {
                                RadioTextButton(
                                    text = it,
                                    selected = currentCharset == state.preferences.subtitleTextEncoding,
                                    onClick = {
                                        onAction(SubtitlePreferencesUiEvent.UpdateSubtitleEncoding(currentCharset))
                                        onAction(SubtitlePreferencesUiEvent.ShowDialog(null))
                                    },
                                )
                            }
                        }
                    }
                }

                SubtitlePreferenceDialog.OpenSubtitlesDialog -> {
                    OpenSubtitlesCredentialsDialog(
                        apiKey = state.preferences.openSubtitlesApiKey,
                        token = state.preferences.openSubtitlesToken,
                        onSave = { apiKey, token ->
                            onAction(
                                SubtitlePreferencesUiEvent.UpdateOpenSubtitlesCredentials(
                                    apiKey,
                                    token,
                                ),
                            )
                            onAction(SubtitlePreferencesUiEvent.ShowDialog(null))
                        },
                        onDismiss = { onAction(SubtitlePreferencesUiEvent.ShowDialog(null)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun OpenSubtitlesCredentialsDialog(
    apiKey: String,
    token: String,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var apiKeyValue by remember(apiKey) { mutableStateOf(apiKey) }
    var tokenValue by remember(token) { mutableStateOf(token) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.opensubtitles)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NextOutlinedTextField(
                    label = { Text(text = stringResource(id = R.string.opensubtitles_api_key)) },
                    value = apiKeyValue,
                    onValueChange = { apiKeyValue = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(id = R.string.opensubtitles_api_key_description),
                    style = MaterialTheme.typography.bodySmall,
                )
                NextOutlinedTextField(
                    label = { Text(text = stringResource(id = R.string.opensubtitles_token)) },
                    value = tokenValue,
                    onValueChange = { tokenValue = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(id = R.string.opensubtitles_token_description),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(apiKeyValue.trim(), tokenValue.trim()) },
                enabled = apiKeyValue.isNotBlank(),
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun SubtitlePreferencesScreenPreview() {
    ResonixTheme {
        SubtitlePreferencesScreenContent(
            state = SubtitlePreferencesUiState(),
            onAction = {},
        )
    }
}
