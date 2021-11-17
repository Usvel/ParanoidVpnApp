package com.paranoid.vpn.app.profile.domain.usecase

import com.paranoid.vpn.app.profile.domain.port.UserFirebase

class UpdateProfileFirebaseUseCase(private val firebase: UserFirebase) {
    suspend fun execute(userName: String, imageUrl : String) {
        firebase.updateProfileUser(userName, imageUrl)
    }
}
