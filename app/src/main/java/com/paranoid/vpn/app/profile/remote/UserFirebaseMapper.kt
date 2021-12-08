package com.paranoid.vpn.app.profile.remote

import com.google.firebase.auth.FirebaseUser
import com.paranoid.vpn.app.common.remote.base.Mapper
import com.paranoid.vpn.app.profile.domain.entity.UserEntity
import javax.inject.Inject

class UserFirebaseMapper @Inject constructor() : Mapper<FirebaseUser?, UserEntity> {
    override fun mapToEntity(obj: FirebaseUser?): UserEntity? {
        return if (obj == null) {
            null
        } else {
            UserEntity(
                email = obj.email,
                name = obj.displayName,
                photoUrl = obj.photoUrl?.toString()
            )
        }
    }
}
