package com.paranoid.vpn.app.profile.di.module

import androidx.lifecycle.ViewModel
import com.paranoid.vpn.app.common.di.keys.ViewModelKey
import com.paranoid.vpn.app.profile.ui.registration.RegistrationViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class RegistrationModule {
    @ViewModelKey(RegistrationViewModel::class)
    @IntoMap
    @Binds
    abstract fun bindsRegistrationViewModule(registrationViewModel: RegistrationViewModel): ViewModel
}
