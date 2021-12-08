package com.paranoid.vpn.app.common.remote

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class FirebaseServiceFactory @Inject constructor() : FirebaseService {
    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun getAuth(): FirebaseAuth {
        return mAuth
    }
}
