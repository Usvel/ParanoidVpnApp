package com.paranoid.vpn.app.profile.domain.usecase

import com.paranoid.vpn.app.profile.domain.port.UserFirebase

class LoginFirebaseUserUseCase(private val firebase: UserFirebase) {
    suspend fun execute(email: String, password: String) {
        firebase.loginUser(email, password)
    }
}
