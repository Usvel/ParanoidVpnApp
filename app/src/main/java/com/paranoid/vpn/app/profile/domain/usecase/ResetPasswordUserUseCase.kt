package com.paranoid.vpn.app.profile.domain.usecase

import com.paranoid.vpn.app.profile.domain.port.UserFirebase
import javax.inject.Inject

class ResetPasswordUserUseCase @Inject constructor(private val firebase: UserFirebase) {
    suspend fun execute(email: String) {
        firebase.passwordResetUser(email)
    }
}
