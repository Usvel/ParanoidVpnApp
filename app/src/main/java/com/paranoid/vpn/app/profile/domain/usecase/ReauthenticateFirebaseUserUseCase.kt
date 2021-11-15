package com.paranoid.vpn.app.profile.domain.usecase

import com.paranoid.vpn.app.profile.domain.port.UserFirebase

class ReauthenticateFirebaseUserUseCase(private val firebase: UserFirebase) {
    suspend fun execute(email: String, password: String) {
        firebase.reauthenticateUser(email, password)
    }
}