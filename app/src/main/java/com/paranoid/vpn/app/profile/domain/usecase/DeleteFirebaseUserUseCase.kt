package com.paranoid.vpn.app.profile.domain.usecase

import com.paranoid.vpn.app.profile.domain.port.UserFirebase

class DeleteFirebaseUserUseCase(private val firebase: UserFirebase) {
    suspend fun execute() {
        return firebase.deleteUser()
    }
}
