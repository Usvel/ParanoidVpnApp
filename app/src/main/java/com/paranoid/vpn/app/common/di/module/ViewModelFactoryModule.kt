package com.paranoid.vpn.app.common.di.module

import androidx.lifecycle.ViewModelProvider
import com.paranoid.vpn.app.common.ui.factory.DaggerViewModelFactory
import dagger.Binds
import dagger.Module

@Module
interface ViewModelFactoryModule {

    @Binds
    fun bindsViewModelFactory(daggerViewModelFactory: DaggerViewModelFactory): ViewModelProvider.Factory
}