package com.resonix.media.feature.player.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.core.net.toUri
import com.resonix.media.core.media.network.opensubtitles.OpenSubtitlesSearchResult
import com.resonix.media.core.media.network.opensubtitles.toOpenSubtitlesLanguage
import com.resonix.media.core.ui.R
import com.resonix.media.core.ui.components.ResonixOutlinedTextField
import com.resonix.media.feature.player.PlayerAction
import com.resonix.media.feature.player.PlayerUiState
import com.resonix.media.feature.player.state.SubtitleOptionsEvent

private const val DEFAULT_LANGUAGE = "eng"

@Composable
fun OnlineSubtitleDialog(
    player: Player,
    state: PlayerUiState,
    onAction: (PlayerAction) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember(player) {
        mutableStateOf(
            player.currentMediaItem?.mediaMetadata?.title?.toString()
                ?: player.currentMediaItem?.mediaId?.toUri()?.lastPathSegment
                ?: "",
        )
    }
    var language by remember {
        mutableStateOf(
            state.playerPreferences.preferredSubtitleLanguage
                .takeIf { it.isNotBlank() }
                ?.toOpenSubtitlesLanguage()
                ?: DEFAULT_LANGUAGE,
        )
    }
    var hasSearched by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.search_subtitles_online)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ResonixOutlinedTextField(
                    label = { Text(text = stringResource(R.string.subtitle_search_query)) },
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                ResonixOutlinedTextField(
                    label = { Text(text = stringResource(R.string.subtitle_language)) },
                    value = language,
                    onValueChange = { language = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = {
                        hasSearched = true
                        onAction(
                            PlayerAction.OnSubtitleOptionEvent(
                                SubtitleOptionsEvent.SearchSubtitles(
                                    query = query.trim(),
                                    language = language.trim(),
                                ),
                            ),
                        )
                    },
                    enabled = query.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.search))
                }
                if (state.isSearchingSubtitles) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
                state.subtitleSearchError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (hasSearched &&
                    !state.isSearchingSubtitles &&
                    state.subtitleSearchResults.isEmpty() &&
                    state.subtitleSearchError == null
                ) {
                    Text(text = stringResource(R.string.subtitle_no_results))
                }
                if (state.subtitleSearchResults.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(state.subtitleSearchResults, key = { it.id }) { result ->
                            OnlineSubtitleResultRow(
                                result = result,
                                onClick = {
                                    onAction(
                                        PlayerAction.OnSubtitleOptionEvent(
                                            SubtitleOptionsEvent.DownloadSubtitle(result),
                                        ),
                                    )
                                },
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(R.string.done))
            }
        },
    )
}

@Composable
private fun OnlineSubtitleResultRow(
    result: OpenSubtitlesSearchResult,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = result.displayName,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = buildString {
                    append(result.attributes.language)
                    result.attributes.featureDetails.year?.let { append(" • $it") }
                    if (result.attributes.hearingImpaired) append(" • SDH")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
