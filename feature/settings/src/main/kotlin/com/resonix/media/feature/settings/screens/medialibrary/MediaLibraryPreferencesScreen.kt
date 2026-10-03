package com.resonix.media.feature.settings.screens.medialibrary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.resonix.media.core.model.ThumbnailGenerationStrategy
import com.resonix.media.core.ui.R
import com.resonix.media.core.ui.components.ClickablePreferenceItem
import com.resonix.media.core.ui.components.ListSectionTitle
import com.resonix.media.core.ui.components.NextTopAppBar
import com.resonix.media.core.ui.components.PreferenceSwitch
import com.resonix.media.core.ui.components.rememberRestorableFocusState
import com.resonix.media.core.ui.components.restorableFocusGroup
import com.resonix.media.core.ui.components.restorableFocusItem
import com.resonix.media.core.ui.components.tvFocusDown
import com.resonix.media.core.ui.designsystem.NextIcons
import com.resonix.media.core.ui.theme.ResonixTheme

@Composable
fun MediaLibraryPreferencesScreen(
    viewModel: MediaLibraryPreferencesViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MediaLibraryPreferencesScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MediaLibraryPreferencesScreenContent(
    state: MediaLibraryPreferencesUiState,
    onAction: (MediaLibraryPreferencesUiEvent) -> Unit,
) {
    val preferences = state.preferences

    val focusState = rememberRestorableFocusState()

    Scaffold(
        topBar = {
            NextTopAppBar(
                title = stringResource(id = R.string.media_library),
                navigationIcon = {
                    FilledTonalIconButton(onClick = { onAction(MediaLibraryPreferencesUiEvent.NavigateUp) }, modifier = Modifier.tvFocusDown(focusState.requester)) {
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
                .restorableFocusGroup(focusState)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            ListSectionTitle(text = stringResource(id = R.string.media_library))
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                PreferenceSwitch(
                    modifier = Modifier.restorableFocusItem(focusState, "mark_last_played"),
                    title = stringResource(id = R.string.mark_last_played_media),
                    description = stringResource(
                        id = R.string.mark_last_played_media_desc,
                    ),
                    icon = NextIcons.Check,
                    isChecked = preferences.markLastPlayedMedia,
                    onClick = { onAction(MediaLibraryPreferencesUiEvent.ToggleMarkLastPlayedMedia) },
                    isFirstItem = true,
                    isLastItem = true,
                )
            }

            ListSectionTitle(text = stringResource(id = R.string.scan))
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                ClickablePreferenceItem(
                    modifier = Modifier.restorableFocusItem(focusState, "manage_folders"),
                    title = stringResource(id = R.string.manage_folders),
                    description = stringResource(id = R.string.manage_folders_desc),
                    icon = NextIcons.FolderOff,
                    onClick = { onAction(MediaLibraryPreferencesUiEvent.OpenFolders) },
                    isFirstItem = true,
                    isLastItem = true,
                )
            }

            ListSectionTitle(text = stringResource(id = R.string.thumbnail))
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                ClickablePreferenceItem(
                    modifier = Modifier.restorableFocusItem(focusState, "thumbnail"),
                    title = stringResource(id = R.string.thumbnail_generation),
                    description = when (preferences.thumbnailGenerationStrategy) {
                        ThumbnailGenerationStrategy.FIRST_FRAME -> stringResource(id = R.string.first_frame)
                        ThumbnailGenerationStrategy.FRAME_AT_PERCENTAGE -> stringResource(R.string.frame_at_position)
                        ThumbnailGenerationStrategy.HYBRID -> stringResource(id = R.string.hybrid)
                    },
                    icon = NextIcons.Image,
                    onClick = { onAction(MediaLibraryPreferencesUiEvent.OpenThumbnails) },
                    isFirstItem = true,
                    isLastItem = true,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun MediaLibraryPreferencesScreenPreview() {
    ResonixTheme {
        MediaLibraryPreferencesScreenContent(
            state = MediaLibraryPreferencesUiState(),
            onAction = {},
        )
    }
}
