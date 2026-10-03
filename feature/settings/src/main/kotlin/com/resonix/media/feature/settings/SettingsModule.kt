package com.resonix.media.feature.settings

import com.resonix.media.core.domain.DomainModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [DomainModule::class])
@ComponentScan
class SettingsModule
