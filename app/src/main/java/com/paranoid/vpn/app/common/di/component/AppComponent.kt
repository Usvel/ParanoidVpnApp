package com.paranoid.vpn.app.common.di.component

import android.content.Context
import com.paranoid.vpn.app.common.di.module.ViewModelFactoryModule
import com.paranoid.vpn.app.profile.di.component.ProfileFeatureComponent
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Component(modules = [ViewModelFactoryModule::class])
@Singleton
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun registerProfileFeatureComponent(): ProfileFeatureComponent.Factory
}
