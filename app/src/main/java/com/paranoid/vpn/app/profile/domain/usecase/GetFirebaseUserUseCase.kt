package com.paranoid.vpn.app.profile.domain.usecase

import com.paranoid.vpn.app.profile.domain.entity.UserEntity
import com.paranoid.vpn.app.profile.domain.port.UserFirebase
import javax.inject.Inject

class GetFirebaseUserUseCase @Inject constructor(private val firebase: UserFirebase) {
    fun execute(): UserEntity? {
        return firebase.getUser()
    }
}
