package com.paranoid.vpn.app.profile.domain.usecase

import com.paranoid.vpn.app.profile.domain.port.UserFirebase
import javax.inject.Inject

class SignOutFirebaseUserUseCase @Inject constructor(private val firebase: UserFirebase) {
    fun execute() {
        firebase.signOutUser()
    }
}
