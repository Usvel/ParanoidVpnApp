package com.paranoid.vpn.app.common.remote

import com.google.firebase.auth.FirebaseAuth

interface FirebaseService {
    fun getAuth(): FirebaseAuth
}
