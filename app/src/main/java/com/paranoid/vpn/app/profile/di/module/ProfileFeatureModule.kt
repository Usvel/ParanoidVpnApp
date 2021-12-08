package com.paranoid.vpn.app.profile.di.module

import com.paranoid.vpn.app.common.remote.FirebaseService
import com.paranoid.vpn.app.common.remote.FirebaseServiceFactory
import com.paranoid.vpn.app.profile.domain.port.UserFirebase
import com.paranoid.vpn.app.profile.remote.UserFirebaseImpl
import dagger.Binds
import dagger.Module

@Module
abstract class ProfileFeatureModule {

    @Binds
    abstract fun bindsUerFirebase(userFirebaseImpl: UserFirebaseImpl): UserFirebase

    @Binds
    abstract fun bindsFirebaseService(profileServiceFactory: FirebaseServiceFactory): FirebaseService
}