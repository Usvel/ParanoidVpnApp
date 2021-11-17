package com.paranoid.vpn.app.profile.domain.usecase

import com.paranoid.vpn.app.profile.domain.port.UserFirebase

class UpdateEmailFirebaseUserUseCase(private val firebase: UserFirebase) {
    suspend fun execute(email: String) {
        firebase.updateEmailUser(email)
    }
}
