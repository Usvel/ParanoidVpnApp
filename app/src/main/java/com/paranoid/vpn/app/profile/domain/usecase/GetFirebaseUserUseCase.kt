package com.paranoid.vpn.app.profile.domain.usecase

import com.paranoid.vpn.app.profile.domain.entity.UserEntity
import com.paranoid.vpn.app.profile.domain.port.UserFirebase

class GetFirebaseUserUseCase(private val firebase: UserFirebase) {
    fun execute(): UserEntity? {
        return firebase.getUser()
    }
}
