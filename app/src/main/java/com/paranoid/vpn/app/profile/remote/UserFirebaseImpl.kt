package com.paranoid.vpn.app.profile.remote

import com.paranoid.vpn.app.common.remote.FirebaseService
import com.paranoid.vpn.app.common.remote.FirebaseServiceFactory
import com.paranoid.vpn.app.profile.domain.entity.UserEntity
import com.paranoid.vpn.app.profile.domain.port.UserFirebase
import kotlinx.coroutines.tasks.await

object UserFirebaseImpl : UserFirebase {
    private val firebaseService: FirebaseService = FirebaseServiceFactory
    private val userFirebaseMapper: UserFirebaseMapper = UserFirebaseMapper

    override fun getUser(): UserEntity? {
        return userFirebaseMapper.mapToEntity(firebaseService.getAuth().currentUser)
    }

    override suspend fun loginUser(email: String, password: String) {
        firebaseService.getAuth().signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun createUser(email: String, password: String) {
        firebaseService.getAuth().createUserWithEmailAndPassword(email, password).await()
    }

    override fun signOut() {
        firebaseService.getAuth().signOut()
    }
}
