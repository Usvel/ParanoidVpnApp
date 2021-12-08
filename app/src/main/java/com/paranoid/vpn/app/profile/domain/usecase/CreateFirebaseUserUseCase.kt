package com.paranoid.vpn.app.profile.domain.usecase

import com.paranoid.vpn.app.profile.domain.port.UserFirebase
import javax.inject.Inject

class CreateFirebaseUserUseCase @Inject constructor(private val firebase: UserFirebase) {
    suspend fun execute(email: String, password: String) {
        return firebase.createUser(email, password)
    }
}
