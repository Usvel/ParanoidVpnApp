package com.paranoid.vpn.app.profile.di.module

import androidx.lifecycle.ViewModel
import com.paranoid.vpn.app.common.di.keys.ViewModelKey
import com.paranoid.vpn.app.profile.ui.edit.EditUserViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class EditUserModule {
    @ViewModelKey(EditUserViewModel::class)
    @IntoMap
    @Binds
    abstract fun bindsEditUserViewModule(editUserViewModel: EditUserViewModel): ViewModel
}