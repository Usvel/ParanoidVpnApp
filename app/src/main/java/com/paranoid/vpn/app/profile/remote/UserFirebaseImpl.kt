package com.paranoid.vpn.app.profile.remote

import android.net.Uri
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
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

    override fun signOutUser() {
        firebaseService.getAuth().signOut()
    }

    override suspend fun updateProfileUser(userName: String, imageUrl: String) {
        firebaseService.getAuth().currentUser?.updateProfile(
            UserProfileChangeRequest.Builder().apply {
                displayName = userName
                photoUri = Uri.parse(imageUrl)
            }.build()
        )?.await()
    }

    override suspend fun updateEmailUser(email: String) {
        firebaseService.getAuth().currentUser?.updateEmail(email)?.await()
    }

    override suspend fun reauthenticateUser(email: String, password: String) {
        firebaseService.getAuth().currentUser?.reauthenticate(
            EmailAuthProvider.getCredential(
                email,
                password
            )
        )?.await()
    }

    override suspend fun deleteUser() {
        firebaseService.getAuth().currentUser?.delete()?.await()
    }

    override suspend fun passwordResetUser(email: String) {
        firebaseService.getAuth().sendPasswordResetEmail(email).await()
    }
}
