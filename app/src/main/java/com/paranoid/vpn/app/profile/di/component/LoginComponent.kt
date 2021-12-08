package com.paranoid.vpn.app.profile.di.component

import com.paranoid.vpn.app.common.di.scope.FragmentScope
import com.paranoid.vpn.app.profile.di.module.LoginModule
import com.paranoid.vpn.app.profile.ui.login.LoginFragment
import dagger.Subcomponent

@Subcomponent(modules = [LoginModule::class])
@FragmentScope
interface LoginComponent {
    fun inject(loginFragment: LoginFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(): LoginComponent
    }
}
