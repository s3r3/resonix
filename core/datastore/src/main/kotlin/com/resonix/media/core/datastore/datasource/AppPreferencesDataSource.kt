package com.resonix.media.core.datastore.datasource

import androidx.datastore.core.DataStore
import com.resonix.media.core.common.Logger
import com.resonix.media.core.common.di.DiQualifiers
import com.resonix.media.core.model.ApplicationPreferences
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory(binds = [])
class AppPreferencesDataSource(
    @Named(DiQualifiers.APP_PREFERENCES) private val appPreferences: DataStore<ApplicationPreferences>,
) : PreferencesDataSource<ApplicationPreferences> {

    companion object {
        private const val TAG = "AppPreferencesDataSource"
    }

    override val preferences = appPreferences.data

    override suspend fun update(
        transform: suspend (ApplicationPreferences) -> ApplicationPreferences,
    ) {
        try {
            appPreferences.updateData(transform)
        } catch (ioException: Exception) {
            Logger.logError(TAG, "Failed to update app preferences: $ioException")
        }
    }
}
