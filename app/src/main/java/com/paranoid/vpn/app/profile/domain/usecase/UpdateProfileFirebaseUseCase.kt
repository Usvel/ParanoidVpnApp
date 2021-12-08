package com.paranoid.vpn.app.profile.domain.usecase

import com.paranoid.vpn.app.profile.domain.port.UserFirebase
import javax.inject.Inject

class UpdateProfileFirebaseUseCase @Inject constructor(private val firebase: UserFirebase) {
    suspend fun execute(userName: String, imageUrl : String) {
        firebase.updateProfileUser(userName, imageUrl)
    }
}
