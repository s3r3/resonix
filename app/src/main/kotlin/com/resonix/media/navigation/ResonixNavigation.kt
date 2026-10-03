package com.resonix.media.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.resonix.media.feature.media.navigation.MediaPickerRoute
import com.resonix.media.feature.media.navigation.mediaPickerEntry
import com.resonix.media.feature.media.navigation.navigateToSearch
import com.resonix.media.feature.media.navigation.navigateToVault
import com.resonix.media.feature.media.navigation.searchEntry
import com.resonix.media.feature.media.navigation.vaultEntry
import com.resonix.media.feature.player.PlayerActivity
import com.resonix.media.feature.player.utils.PlayerApi
import com.resonix.media.feature.settings.Setting
import com.resonix.media.feature.settings.navigation.aboutPreferencesEntry
import com.resonix.media.feature.settings.navigation.appearancePreferencesEntry
import com.resonix.media.feature.settings.navigation.audioPreferencesEntry
import com.resonix.media.feature.settings.navigation.generalPreferencesEntry
import com.resonix.media.feature.settings.navigation.gesturePreferencesEntry
import com.resonix.media.feature.settings.navigation.librariesEntry
import com.resonix.media.feature.settings.navigation.mediaLibraryPreferencesEntry
import com.resonix.media.feature.settings.navigation.folderPreferencesEntry
import com.resonix.media.feature.settings.navigation.navigateToAboutPreferences
import com.resonix.media.feature.settings.navigation.navigateToAppearancePreferences
import com.resonix.media.feature.settings.navigation.navigateToAudioPreferences
import com.resonix.media.feature.settings.navigation.navigateToGeneralPreferences
import com.resonix.media.feature.settings.navigation.navigateToGesturePreferences
import com.resonix.media.feature.settings.navigation.navigateToLibraries
import com.resonix.media.feature.settings.navigation.navigateToMediaLibraryPreferencesScreen
import com.resonix.media.feature.settings.navigation.navigateToFolderPreferencesScreen
import com.resonix.media.feature.settings.navigation.navigateToPlayerPreferences
import com.resonix.media.feature.settings.navigation.navigateToSettings
import com.resonix.media.feature.settings.navigation.navigateToSubtitlePreferences
import com.resonix.media.feature.settings.navigation.navigateToThumbnailPreferencesScreen
import com.resonix.media.feature.settings.navigation.playerPreferencesEntry
import com.resonix.media.feature.settings.navigation.settingsEntry
import com.resonix.media.feature.settings.navigation.subtitlePreferencesEntry
import com.resonix.media.feature.settings.navigation.thumbnailPreferencesEntry

private fun NavBackStack<NavKey>.popSettings() {
    if (size > 1) removeAt(lastIndex)
}

fun EntryProviderScope<NavKey>.resonixMediaNavGraph(
    context: Context,
    backStack: NavBackStack<NavKey>,
) {
    mediaPickerEntry(
        onNavigateUp = { backStack.popSettings() },
        onPlayVideo = { uri -> context.startPlayback(uri) },
        onPlayVideos = { uris -> context.startPlayback(uris) },
        onFolderClick = { folderId -> backStack.add(MediaPickerRoute(folderId)) },
        onSettingsClick = { backStack.navigateToSettings() },
        onSearchClick = { backStack.navigateToSearch() },
        onVaultClick = { backStack.navigateToVault() },
    )
    searchEntry(
        onNavigateUp = { backStack.popSettings() },
        onPlayVideo = { uri -> context.startPlayback(uri) },
        onFolderClick = { folderId -> backStack.add(MediaPickerRoute(folderId)) },
    )
    vaultEntry(
        onNavigateUp = { backStack.popSettings() },
        onPlayVideo = { uri -> context.startPlayback(uri) },
        onPlayVideos = { uris -> context.startPlayback(uris) },
    )
    settingsEntry(
        onNavigateUp = { backStack.popSettings() },
        onItemClick = { setting ->
            when (setting) {
                Setting.APPEARANCE -> backStack.navigateToAppearancePreferences()
                Setting.MEDIA_LIBRARY -> backStack.navigateToMediaLibraryPreferencesScreen()
                Setting.PLAYER -> backStack.navigateToPlayerPreferences()
                Setting.GESTURES -> backStack.navigateToGesturePreferences()
                Setting.AUDIO -> backStack.navigateToAudioPreferences()
                Setting.SUBTITLE -> backStack.navigateToSubtitlePreferences()
                Setting.GENERAL -> backStack.navigateToGeneralPreferences()
                Setting.ABOUT -> backStack.navigateToAboutPreferences()
            }
        },
    )
    appearancePreferencesEntry(onNavigateUp = { backStack.popSettings() })
    mediaLibraryPreferencesEntry(
        onNavigateUp = { backStack.popSettings() },
        onFolderSettingClick = { backStack.navigateToFolderPreferencesScreen() },
        onThumbnailSettingClick = { backStack.navigateToThumbnailPreferencesScreen() },
    )
    folderPreferencesEntry(onNavigateUp = { backStack.popSettings() })
    thumbnailPreferencesEntry(onNavigateUp = { backStack.popSettings() })
    playerPreferencesEntry(onNavigateUp = { backStack.popSettings() })
    gesturePreferencesEntry(onNavigateUp = { backStack.popSettings() })
    audioPreferencesEntry(onNavigateUp = { backStack.popSettings() })
    subtitlePreferencesEntry(onNavigateUp = { backStack.popSettings() })
    generalPreferencesEntry(onNavigateUp = { backStack.popSettings() })
    aboutPreferencesEntry(
        onLibrariesClick = { backStack.navigateToLibraries() },
        onNavigateUp = { backStack.popSettings() },
    )
    librariesEntry(onNavigateUp = { backStack.popSettings() })
}

fun Context.startPlayback(uri: Uri) {
    val intent = Intent(this, PlayerActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        data = uri
    }
    startActivity(intent)
}

fun Context.startPlayback(uris: List<Uri>, startUri: Uri? = null) {
    val uri = startUri?.takeIf(uris::contains) ?: uris.firstOrNull() ?: return
    val intent = Intent(this, PlayerActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        data = uri
        putParcelableArrayListExtra(PlayerApi.API_PLAYLIST, ArrayList(uris))
    }
    startActivity(intent)
}
