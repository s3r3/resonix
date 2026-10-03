package com.resonix.media.app

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.request.CachePolicy
import coil3.request.crossfade
import com.resonix.media.core.common.di.DispatchersModule
import com.resonix.media.core.data.DataModule
import com.resonix.media.core.data.repository.PreferencesRepository
import com.resonix.media.core.media.MediaModule
import com.resonix.media.core.media.vault.VaultCipher
import com.resonix.media.core.model.ThumbnailGenerationStrategy
import com.resonix.media.ThumbnailStrategy
import com.resonix.media.VaultImageFetcher
import com.resonix.media.VideoThumbnailDecoder
import com.resonix.media.feature.media.MediaModule as MediaFeatureModule
import com.resonix.media.feature.network.NetworkModule
import com.resonix.media.feature.player.PlayerModule
import com.resonix.media.feature.playlist.PlaylistModule
import com.resonix.media.feature.settings.SettingsModule
import okio.FileSystem
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(
    includes = [
        DispatchersModule::class,
        DataModule::class,
        MediaModule::class,
        MediaFeatureModule::class,
        NetworkModule::class,
        PlaylistModule::class,
        PlayerModule::class,
        SettingsModule::class,
    ],
)
object AppModule {
    @Single
    fun provideImageLoader(
        context: Context,
        vaultCipher: VaultCipher,
        preferencesRepository: PreferencesRepository,
    ): ImageLoader =
        ImageLoader.Builder(context)
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache(
                DiskCache.Builder()
                    .fileSystem(FileSystem.SYSTEM)
                    .directory(context.filesDir.resolve("thumbnails"))
                    .maxSizePercent(1.0)
                    .build(),
            )
            .components {
                // Vault bytes are encrypted, so they are decrypted before the frame is pulled.
                add(VaultImageFetcher.Factory(vaultCipher, context))
                add(
                    VideoThumbnailDecoder.Factory(
                        thumbnailStrategy = {
                            val preferences = preferencesRepository.applicationPreferences.value
                            when (preferences.thumbnailGenerationStrategy) {
                                ThumbnailGenerationStrategy.FIRST_FRAME -> ThumbnailStrategy.FirstFrame
                                ThumbnailGenerationStrategy.FRAME_AT_PERCENTAGE,
                                ThumbnailGenerationStrategy.HYBRID ->
                                    ThumbnailStrategy.Hybrid(preferences.thumbnailFramePosition)
                            }
                        },
                    ),
                )
            }
            .crossfade(true)
            .build()
}