package com.paranoid.vpn.app.common.remote

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

object GoogleServiceFactory: GoogleService {

    @SuppressLint("StaticFieldLeak")
    private lateinit var googleSignInClient: GoogleSignInClient

    fun makeGoogleSignInClient(context: Context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("939346116267-frv4mq2d27lt5cpanp1gkeue68k5kr9g.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    override fun getGoogleSignInClient(): GoogleSignInClient {
        return googleSignInClient
    }
}