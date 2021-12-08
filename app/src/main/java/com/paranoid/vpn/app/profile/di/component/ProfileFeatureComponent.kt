package com.paranoid.vpn.app.profile.di.component

import com.paranoid.vpn.app.profile.di.module.ProfileFeatureModule
import com.paranoid.vpn.app.profile.di.scope.ProfileScope
import dagger.Subcomponent

@Subcomponent(modules = [ProfileFeatureModule::class])
@ProfileScope
interface ProfileFeatureComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(): ProfileFeatureComponent
    }

    fun registerLoginComponent(): LoginComponent.Factory

    fun registerEditUserComponent(): EditUserComponent.Factory

    fun registerProfileComponent(): ProfileComponent.Factory

    fun registerRegistrationComponent(): RegistrationComponent.Factory
}
