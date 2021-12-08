package com.paranoid.vpn.app.profile.di.component

import com.paranoid.vpn.app.common.di.scope.FragmentScope
import com.paranoid.vpn.app.profile.di.module.ProfileModule
import com.paranoid.vpn.app.profile.ui.profile.ProfileFragment
import com.paranoid.vpn.app.profile.ui.profile.ProfileViewModel
import dagger.Subcomponent

@Subcomponent(modules = [ProfileModule::class])
@FragmentScope
interface ProfileComponent {
    fun inject(profileFragment: ProfileFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(): ProfileComponent
    }
}
