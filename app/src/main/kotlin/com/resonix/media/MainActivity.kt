package com.resonix.media

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resonix.media.core.data.repository.PreferencesRepository
import com.resonix.media.core.model.ThemeConfig
import org.koin.compose.koinInject
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import com.resonix.media.core.ui.theme.ResonixTheme
import com.resonix.media.feature.media.navigation.MediaPickerRoute
import com.resonix.media.navigation.resonixMediaNavGraph
import com.resonix.media.splash.SplashVideoScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val preferencesRepository: PreferencesRepository = koinInject()
            val preferences by preferencesRepository.applicationPreferences.collectAsStateWithLifecycle(
                initialValue = com.resonix.media.core.model.ApplicationPreferences(),
            )
            val darkTheme = when (preferences.themeConfig) {
                ThemeConfig.ON -> true
                ThemeConfig.OFF -> false
                ThemeConfig.SYSTEM -> isSystemInDarkTheme()
            }
            ResonixTheme(
                darkTheme = darkTheme,
                highContrastDarkTheme = preferences.useHighContrastDarkTheme,
                dynamicColor = preferences.useDynamicColors,
                accentColor = preferences.accentColor,
            ) {
                var splashFinished by rememberSaveable { mutableStateOf(false) }
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!splashFinished) {
                        SplashVideoScreen(onFinished = { splashFinished = true })
                        return@Surface
                    }
                    val backStack = rememberNavBackStack(MediaPickerRoute())
                    val provider = entryProvider {
                        resonixMediaNavGraph(this@MainActivity, backStack)
                    }
                    val entries = rememberDecoratedNavEntries(
                        backStack = backStack,
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                        ),
                        entryProvider = provider,
                    )
                    NavDisplay(
                        entries = entries,
                        onBack = {
                            if (backStack.size > 1) {
                                backStack.removeAt(backStack.lastIndex)
                            }
                        },
                    )
                }
            }
        }
    }
}
