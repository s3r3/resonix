package com.resonix.media

import android.app.Application
import coil3.SingletonImageLoader
import coil3.PlatformContext
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin

@KoinApplication(modules = [com.resonix.media.app.AppModule::class])
class ResonixApplication :
    Application(),
    SingletonImageLoader.Factory {

    private val imageLoader: coil3.ImageLoader by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin<ResonixApplication> {
            androidContext(this@ResonixApplication)
        }
    }

    override fun newImageLoader(context: PlatformContext): coil3.ImageLoader = imageLoader
}