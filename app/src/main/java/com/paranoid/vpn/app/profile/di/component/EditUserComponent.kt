package com.paranoid.vpn.app.profile.di.component

import com.paranoid.vpn.app.common.di.scope.FragmentScope
import com.paranoid.vpn.app.profile.di.module.EditUserModule
import com.paranoid.vpn.app.profile.ui.edit.EditUserFragment
import dagger.Subcomponent

@Subcomponent(modules = [EditUserModule::class])
@FragmentScope
interface EditUserComponent {
    fun inject(editUserComponent: EditUserFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(): EditUserComponent
    }
}
