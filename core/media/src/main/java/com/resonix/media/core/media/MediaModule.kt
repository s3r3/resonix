package com.resonix.media.core.media

import com.resonix.media.core.common.di.DispatchersModule
import com.resonix.media.core.database.DatabaseModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [DatabaseModule::class, DispatchersModule::class])
@ComponentScan
class MediaModule
