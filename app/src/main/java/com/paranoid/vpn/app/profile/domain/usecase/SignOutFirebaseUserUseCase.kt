package com.paranoid.vpn.app.profile.domain.usecase

import com.paranoid.vpn.app.profile.domain.port.UserFirebase

class SignOutFirebaseUserUseCase(private val firebase: UserFirebase) {
    fun execute() {
        firebase.signOutUser()
    }
}
