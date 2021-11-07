package com.paranoid.vpn.app.common.remote

import com.google.firebase.auth.FirebaseAuth

object FirebaseServiceFactory : FirebaseService {
    lateinit var mAuth: FirebaseAuth

    fun makeFirebase() {
        mAuth = FirebaseAuth.getInstance()
    }

    override fun getAuth(): FirebaseAuth {
        return mAuth
    }
}
