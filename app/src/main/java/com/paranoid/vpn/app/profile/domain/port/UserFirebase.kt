package com.paranoid.vpn.app.profile.domain.port

import com.paranoid.vpn.app.profile.domain.entity.UserEntity

interface UserFirebase {
    fun getUser(): UserEntity?

    suspend fun loginUser(email: String, password: String)

    suspend fun createUser(email: String, password: String)

    fun signOut()
}
