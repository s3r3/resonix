package com.resonix.media.core.data

import com.resonix.media.core.data.repository.NetworkConnectionRepository
import com.resonix.media.core.database.DatabaseModule
import com.resonix.media.core.datastore.di.DataStoreModule
import com.resonix.media.core.media.MediaModule
import com.resonix.media.core.media.network.NetworkConnectionResolver
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [DatabaseModule::class, DataStoreModule::class, MediaModule::class])
@ComponentScan
class DataModule {
    /** Lets `core:media` resolve a playback uri's connection id without depending on `core:data`. */
    @Single
    fun providesNetworkConnectionResolver(
        repository: NetworkConnectionRepository,
    ): NetworkConnectionResolver = NetworkConnectionResolver { id -> repository.getConnection(id) }
}
