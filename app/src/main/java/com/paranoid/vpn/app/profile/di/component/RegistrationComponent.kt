package com.paranoid.vpn.app.profile.di.component

import com.paranoid.vpn.app.common.di.scope.FragmentScope
import com.paranoid.vpn.app.profile.di.module.RegistrationModule
import com.paranoid.vpn.app.profile.ui.profile.ProfileFragment
import com.paranoid.vpn.app.profile.ui.registration.RegistrationFragment
import dagger.Subcomponent

@Subcomponent(modules = [RegistrationModule::class])
@FragmentScope
interface RegistrationComponent {
    fun inject(registrationFragment: RegistrationFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(): RegistrationComponent
    }
}
